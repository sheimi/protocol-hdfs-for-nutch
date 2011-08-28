package me.sheimi.nutch.hdfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.net.protocols.HttpDateFormat;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.MimeUtil;

public class HdfsResponse {

	private String orig;
	private String base;
	private byte[] content;
	private static final byte[] EMPTY_CONTENT = new byte[0];
	private int code;
	private Metadata headers = new Metadata();

	private final Hdfs hdfs;
	private Configuration conf;

	private MimeUtil MIME;

	public HdfsResponse(URL url, CrawlDatum datum, Hdfs hdfs, Configuration conf)
			throws HdfsException {

		this.orig = url.toString();
		this.base = url.toString();
		this.hdfs = hdfs;
		this.conf = conf;

		MIME = new MimeUtil(conf);

		// test the protocol head (not support currently)
		// if (!"hdfs".equals(url.getProtocol()))
		// throw new HdfsException("Not a hdfs url:" + url);

		if (Hdfs.LOG.isTraceEnabled()) {
			Hdfs.LOG.trace("fetching" + url);
		}

		if (url.getPath() != url.getFile()) {
			if (Hdfs.LOG.isWarnEnabled()) {
				Hdfs.LOG.warn("url.getPath() != url.getFile(): " + url);
			}
		}

		String path = "".equals(url.getPath()) ? "/" : url.getPath();
		try {
			path = java.net.URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}

		// TODO

		try {
			this.content = null;
			FileSystem fs = FileSystem.get(new Configuration());
			Path fsPath = new Path(path);

			FileStatus fileStatus = fs.getFileStatus(fsPath);

			// ? redirect ??

			if (fileStatus.isDir()) {
				getHdfsDirAsHttpResponse(fs, fsPath, fileStatus);
			} else {
				getHdfsFileAsHttpResponse(fs, fsPath, fileStatus);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			this.code = 404;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			this.code = 500;
			return;
		}
	}

	public int getCode() {
		return code;
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public byte[] getContent() {
		return content;
	}

	public Content toContent() {
		return new Content(orig, base, (content != null ? content
				: EMPTY_CONTENT), getHeader(Response.CONTENT_TYPE), headers,
				this.conf);
	}

	private void getHdfsDirAsHttpResponse(FileSystem fs, Path fsPath,
			FileStatus fileStatus) {
		// TODO will be implement soon
	}

	private void getHdfsFileAsHttpResponse(FileSystem fs, Path fsPath,
			FileStatus fileStatus) throws HdfsException, IOException {
		long size = fileStatus.getLen();
		if (size > Integer.MAX_VALUE) {
			throw new HdfsException("file is too large, size: " + size);
		}

		int len = (int) size;

		if (this.hdfs.maxContentLength >= 0 && len > this.hdfs.maxContentLength)
			len = this.hdfs.maxContentLength;

		this.content = new byte[len];

		FSDataInputStream is = fs.open(fsPath);
		int offset = 0;
		int n = 0;
		while (offset < len
				&& (n = is.read(this.content, offset, len - offset)) >= 0) {
			offset += n;
		}
		is.close();

		headers.set(Response.CONTENT_LENGTH, new Long(size).toString());
		headers.set(Response.LAST_MODIFIED,
				HttpDateFormat.toString(fileStatus.getModificationTime()));

		// TOD MimeType
		headers.set(Response.CONTENT_TYPE, "text/html");
		this.code = 200;
	}
	
}
