package me.sheimi.nutch.hdfs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.nutch.crawl.CrawlDatum;
import org.apache.hadoop.io.Text;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.net.protocols.HttpDateFormat;
import org.apache.nutch.net.protocols.Response;

import org.apache.hadoop.conf.Configuration;

import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.EmptyRobotRules;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolOutput;
import org.apache.nutch.protocol.ProtocolStatus;
import org.apache.nutch.protocol.RobotRules;
import org.apache.nutch.util.NutchConfiguration;

import java.net.URL;

public class Hdfs implements Protocol {

	public static final Log LOG = LogFactory.getLog(Hdfs.class);
	static final int MAX_REDIRECTS = 5;

	int maxContentLength;
	boolean crawlParents;

	private Configuration conf;

	// constructor
	public Hdfs() {
	}

	public void setMaxContentLength(int length) {
		this.maxContentLength = length;
	}

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
		this.maxContentLength = conf.getInt("file.content.limit", 64 * 1024);
		this.crawlParents = conf.getBoolean("file.crawl.parent", true);
	}

	@Override
	public ProtocolOutput getProtocolOutput(Text url, CrawlDatum datum) {
		// TODO Auto-generated method stub
		String urlString = url.toString();
		try {
			URL u = new URL(urlString);

			int redirects = 0;

			while (true) {
				HdfsResponse response;
				response = new HdfsResponse(u, datum, this, getConf());

				int code = response.getCode();

				if (code == 200) {
					return new ProtocolOutput(response.toContent());
				} else if (code >= 300 && code < 400) {
					if (redirects == MAX_REDIRECTS)
						throw new HdfsException("Too many redirects: " + url);
					u = new URL(response.getHeader("Location"));
					redirects++;
					if (LOG.isTraceEnabled()) {
						LOG.trace("redirect to " + u);
					}
				} else {
					throw new HdfsError(code);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ProtocolOutput(null, new ProtocolStatus(e));
		}
	}

	@Override
	public RobotRules getRobotRules(Text arg0, CrawlDatum arg1) {
		return EmptyRobotRules.RULES;
	}

}
