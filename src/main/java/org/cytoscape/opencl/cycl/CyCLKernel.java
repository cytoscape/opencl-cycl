package org.cytoscape.opencl.cycl;

import java.nio.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.lwjgl.system.*;


import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opencl.CL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class CyCLKernel
{
  private Boolean finalized = false;

  private long kernel;
  private CyCLContext context;

  public CyCLKernel(CyCLContext context, CyCLProgram program, String kernelName)
  {
    this.context = context;

    IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
    kernel = clCreateKernel(program.getProgram(), kernelName, errorBuffer);
    CyCLUtils.checkCLError(errorBuffer.get(0));
  }

  public long getKernel() { return kernel; }

  public void execute(long[] dimsGlobal, long[] dimsLocal, Object... args)
  {
    executeWithOffset(dimsGlobal, dimsLocal, null, args);
  }

  public void executeWithOffset(long[] dimsGlobal, long[] dimsLocal, long globalOffset[], Object... args)
  {
    synchronized (CyCL.sync)
    {
      int a = 0;
      for(Object arg : args)
      {
        if(arg.getClass().equals(CyCLLocalSize.class))
        {
          // We're allocating local memory
          CyCLLocalSize localSize = (CyCLLocalSize)arg;
          // CyCLBuffer bufferLocal = localSize.getBuffer(context);
          // CyCLBuffer bufferLocal = new CyCLBuffer(context, byte.class, localSize.getSize().intValue(), CL_MEM_READ_ONLY|CL_MEM_ALLOC_HOST_PTR);
          CyCLUtils.checkCLError(clSetKernelArg(kernel, a++, localSize.getSize().longValue()), "clSetKernelArg[CyCLLocalSize]");
        }
        else if(arg.getClass().equals(CyCLBuffer.class))
        {
          // System.out.println((CyCLBuffer)arg);
          // clSetKernelArg(kernel, a++, ((CyCLBuffer)arg).getMemObject());
          CyCLUtils.checkCLError(clSetKernelArg1p(kernel, a++, ((CyCLBuffer)arg).getMemObject()), "clSetKernelArg1p");
        }
        else
        {
          // System.out.println("Setting arg: "+arg+" class is "+arg.getClass().toString());
          if(arg.getClass().equals(Byte.class))
            CyCLUtils.checkCLError(clSetKernelArg1b(kernel, a++, (byte)arg),"clSetKernelArg1b");
          else if(arg.getClass().equals(Short.class))
            CyCLUtils.checkCLError(clSetKernelArg1s(kernel, a++, (short)arg),"clSetKernelArg1b");
          else if(arg.getClass().equals(Integer.class)) {
            // System.out.println("Setting integer value");
            CyCLUtils.checkCLError(clSetKernelArg1i(kernel, a++, (int)arg),"clSetKernelArg1i");
          } else if(arg.getClass().equals(Long.class))
            CyCLUtils.checkCLError(clSetKernelArg1l(kernel, a++, (long)arg),"clSetKernelArg1l");
          else if(arg.getClass().equals(Float.class))
            CyCLUtils.checkCLError(clSetKernelArg1f(kernel, a++, (float)arg),"clSetKernelArg1f");
          else if(arg.getClass().equals(Double.class))
            CyCLUtils.checkCLError(clSetKernelArg1d(kernel, a++, (double)arg),"clSetKernelArg1d");
        }
      }

      PointerBuffer bufferGlobal = BufferUtils.createPointerBuffer(dimsGlobal.length);
      for (int i = 0; i < dimsGlobal.length; i++)
        bufferGlobal.put(i, dimsGlobal[i]);

      PointerBuffer bufferGlobalOffset = null;
      if (globalOffset != null)
      {
        if(globalOffset.length != dimsGlobal.length)
        {
          throw new CyCLException("Global offset and global dimensions must have the same length");
        }
        bufferGlobalOffset = BufferUtils.createPointerBuffer(globalOffset.length);
        for (int i = 0; i < globalOffset.length; i++)
        {
          bufferGlobalOffset.put(i, globalOffset[i]);
        }
      }

      PointerBuffer bufferLocal = null;
      if(dimsLocal != null)
      {
        bufferLocal = PointerBuffer.allocateDirect(dimsLocal.length);
        for (int i = 0; i < dimsLocal.length; i++)
          bufferLocal.put(i, dimsLocal[i]);
      }

      try
      {
        CyCLUtils.checkCLError(clEnqueueNDRangeKernel(context.getQueue(), kernel, dimsGlobal.length, bufferGlobalOffset, bufferGlobal, bufferLocal, null, null));
        clFinish(context.getQueue());
      }
      catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public int getKernelInfoInt(int param_name) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pl = stack.mallocInt(1);
            CyCLUtils.checkCLError(clGetKernelInfo(this.kernel, param_name, pl, null));
            return pl.get(0);
        }
    }

  public long getKernelInfoPointer(int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            CyCLUtils.checkCLError(clGetKernelInfo(this.kernel, param_name, pp, null));
            return pp.get(0);
        }
    }

  public String getKernelInfoStringUTF8(int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            CyCLUtils.checkCLError(clGetKernelInfo(this.kernel, param_name, (ByteBuffer)null, pp));
            int bytes = (int)pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            CyCLUtils.checkCLError(clGetKernelInfo(this.kernel, param_name, buffer, null));

            return memUTF8(buffer, bytes - 1);
        }
  }
    

  @Override
  protected void finalize()
  {
    try
    {
      if(finalized)
        return;

      CyCLUtils.checkCLError(clReleaseKernel(kernel));

      finalized = true;
      super.finalize();
    }
    catch (Throwable exc)
    {
      System.out.println("Could not finalize CyCLKernel " + kernel + ": " + exc.getMessage());
      throw new RuntimeException("Could not finalize CyCLKernel object.");
    }
  }
}
