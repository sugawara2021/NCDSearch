package ncdsearch.comparison.ncd;

import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

public class XzStrategy implements ICompressionStrategy {

	private LZMA2Options xzOptions;
	
	
	public XzStrategy() {
		try {
			xzOptions = new LZMA2Options(0);
		} catch (UnsupportedOptionsException e) {
		}
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		DataSizeRecordStream sizeRecorder = new DataSizeRecordStream();
		try {
			//XZCompressorOutputStream stream = new XZCompressorOutputStream(sizeRecorder);
			//LZMACompressorOutputStream stream = new LZMACompressorOutputStream(sizeRecorder);
			XZOutputStream stream = new XZOutputStream(sizeRecorder, xzOptions);
			stream.write(buf, start, length);
			stream.close();
			return sizeRecorder.size();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public void close() {
	}
	
}
