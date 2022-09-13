package org.cytoscape.opencl.cycl;

import java.nio.*;

import org.lwjgl.opencl.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opencl.CL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/***
 * Provides functionality associated with an OpenCL memory object.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCLBuffer 
{
  private CyCLContext context;
  private Class<?> type;
  private int elements;
  private long memObject;
  private ByteBuffer buffer;
  private boolean finalized;

  /***
   * Allocates [sizeof(type) * elements] bytes in device memory without copying any host data
   * 
   * @param context The context of the device where the memory should be allocated
   * @param type Element type
   * @param elements Number of elements
   */
  public CyCLBuffer(CyCLContext context, Class<?> type, int elements)
  {
    this.context = context;
    this.type = type;
    this.elements = elements;

    // Create the buffer
    buffer = BufferUtils.createByteBuffer(sizeInBytes());
  }

  // Create read buffer
  public CyCLBuffer(CyCLContext context, Class<?> type, int elements, int bits) {
    this(context, type, elements);
    createClBuffer(bits);
  }
  
  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, byte[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, byte[] data, int bits)
  {
    this(context, byte.class, data.length);

    // Load the buffer
    buffer.put(data);
    buffer.rewind();

    createClBuffer(bits);
  }

  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, short[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, short[] data, int bits)
  {
    this(context, short.class, data.length);
    buffer.asShortBuffer().put(data);
    buffer.rewind();

    createClBuffer(bits);
  }

  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, int[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, int[] data, int bits)
  {
    this(context, int.class, data.length);

    // Create the buffer
    buffer = BufferUtils.createByteBuffer(sizeInBytes());

    // Load the buffer
    buffer.asIntBuffer().put(data);
    buffer.rewind();

    createClBuffer(bits);
  }

  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, long[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, long[] data, int bits)
  {
    this(context, long.class, data.length);

    // Load the buffer
    buffer.asLongBuffer().put(data);
    buffer.rewind();

    createClBuffer(bits);
  }

  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, float[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, float[] data, int bits)
  {
    this(context, float.class, data.length);
    buffer.asFloatBuffer().put(data);
    buffer.rewind();

    createClBuffer(bits);
  }

  /***
   * Allocates device memory to fit all elements in data, and copies its contents
   * 
   * @param context The context of the device where the memory should be allocated
   * @param data Data to be copied into device memory
   */
  public CyCLBuffer(CyCLContext context, double[] data) {
    this(context, data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
  }

  public CyCLBuffer(CyCLContext context, double[] data, int bits)
  {
    this(context, double.class, data.length);
    buffer.asDoubleBuffer().put(data);
    buffer.rewind();

    createClBuffer(bits);
  }
  
  /***
   * Returns the number of bytes associated with the CyCLBuffer's element type.
   * 
   * @return sizeof(type)
   */
  public int elementSize()
  {
    if(type.equals(char.class))
      return Sizeof.cl_char;
    else if(type.equals(byte.class))
      return Sizeof.cl_char;
    else if(type.equals(short.class))
      return Sizeof.cl_short;
    else if(type.equals(float.class))
      return Sizeof.cl_float;
    else if(type.equals(int.class))
      return Sizeof.cl_int;
    else if(type.equals(double.class))
      return Sizeof.cl_double;
    else if(type.equals(long.class))
      return Sizeof.cl_long;
    else
      return 1;
  }
  
  /***
   * Returns the overall buffer size in bytes.
   * 
   * @return sizeof(type) * elements
   */
  public int sizeInBytes()
  {
    return elementSize() * elements;
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(byte[] data, int length, int offset)
  {
    buffer.put(data);
    buffer.rewind();
    CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null);
    CL10.clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(short[] data, int length, int offset)
  {
    buffer.asShortBuffer().put(data);
    buffer.rewind();
    CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, data, null, null);
    CL10.clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(int[] data, int length, int offset)
  {
    buffer.asIntBuffer().put(data);
    buffer.rewind();
    CyCLUtils.checkCLError(clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null));
    clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(long[] data, int length, int offset)
  {
    buffer.asLongBuffer().put(data);
    buffer.rewind();
    CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null);
    CL10.clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(float[] data, int length, int offset)
  {
    buffer.asFloatBuffer().put(data);
    buffer.rewind();
    CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, data, null, null);
    CL10.clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   * @param length Number of elements to be copied
   * @param offset Offset in bytes from the device memory's start
   */
  public void setFromHost(double[] data, int length, int offset)
  {
    buffer.asDoubleBuffer().put(data);
    buffer.rewind();
    CL10.clEnqueueWriteBuffer(context.getQueue(), memObject, true, (long)offset, data, null, null);
    CL10.clFinish(context.getQueue());
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(byte[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(short[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(int[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(long[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(float[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from host to device memory.
   * 
   * @param data Data to be copied
   */
  public void setFromHost(double[] data)
  {
    setFromHost(data, sizeInBytes(), 0);
  }
  
  /***
   * Copies data from another device buffer to this one.
   * 
   * @param src Device buffer with the source data
   * @param bytes Amount of bytes to be copied
   * @param offsetSrc Offset in bytes from the start of the source buffer
   * @param offsetDst Offset in bytes from the start of the destination buffer
   */
  public void setFromDevice(CyCLBuffer src, long bytes, long offsetSrc, long offsetDst)
  {
    CL10.clEnqueueCopyBuffer(context.getQueue(), src.getMemObject(), memObject, offsetSrc, offsetDst, bytes, null, null);
  }

  /***
   * Copies data from another device buffer to this one.
   * 
   * @param src Device buffer with the source data
   */
  public void setFromDevice(CyCLBuffer src)
  {
    setFromDevice(src, sizeInBytes(), 0, 0);
  }

  public void getFromDevice(long offset) {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, offset, buffer, null, null), "clEnqueueReadBuffer");
  }
  
  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(byte[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(short[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.asShortBuffer().get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(int[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.asIntBuffer().get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(long[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.asLongBuffer().get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(float[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.asFloatBuffer().get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   * @param length Number of elements
   * @param offset Offset in bytes from the start of the device buffer
   */
  public void getFromDevice(double[] data, int length, int offset)
  {
    CyCLUtils.checkCLError(CL10.clEnqueueReadBuffer(context.getQueue(), memObject, true, (long)offset, buffer, null, null), "clEnqueueReadBuffer");
    buffer.asDoubleBuffer().get(data);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(byte[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(short[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(int[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(long[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(float[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  /***
   * Copies data from device to host memory.
   * 
   * @param data Array that the data will be copied to
   */
  public void getFromDevice(double[] data)
  {
    getFromDevice(data, sizeInBytes(), 0);
  }

  public void createClBuffer(int bits) {
    // Queue it
    IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
    if ((bits & CL_MEM_READ_ONLY) != 0) {
      memObject = CL10.clCreateBuffer(context.getContext(), bits, sizeInBytes(), errorBuffer);
      CyCLUtils.checkCLError(errorBuffer.get(0), "clCreateBuffer");
    } else {
      memObject = CL10.clCreateBuffer(context.getContext(), bits, buffer, errorBuffer);
      CyCLUtils.checkCLError(errorBuffer.get(0), "clCreateBuffer");
    }
  }
  
  /***
   * Gets the underlying memory object.
   * 
   * @return LWJGL memory object
   */
  public long getMemObject()
  {
    return memObject;
  }
  
  /***
   * Gets the underlying buffer.
   * 
   * @return ByteBuffer
   */
  public ByteBuffer getBuffer()
  {
    return buffer;
  }
  
  /***
   * Frees all device memory associated with the buffer.
   * CyCLBuffer cannot be used anymore once this method has been executed.
   */
  public void free()
  {
    memFree(buffer);
    this.finalize();
  }
  
  /***
   * Frees all device memory associated with the buffer.
   * CyCLBuffer cannot be used anymore once this method has been executed.
   */
  @Override
  protected void finalize() 
  {
    try
    {
      if(finalized)
        return;
      
      CyCLUtils.checkCLError(CL10.clReleaseMemObject(memObject));
      
      finalized = true;
      super.finalize();
    } 
    catch (Throwable exc) 
    {       
      System.out.println("Could not finalize CyCLBuffer size " + sizeInBytes() + ": " + exc.getMessage());
      throw new RuntimeException("Could not finalize CyCLBuffer object.");
    }
  }

  @Override
  public String toString() {
    return "CyCLBuffer[type="+type.toString()+",size="+elements+",buffer="+buffer+"]";
  }
}
