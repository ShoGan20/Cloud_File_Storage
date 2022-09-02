package fileSync.model;

import java.net.InetAddress;

public class Address implements Comparable<Address> {

	private InetAddress host;
	private int port;

	public Address(InetAddress host, int port) {

		this.host = host;
		this.port = port;

	}

	public InetAddress getHost() {

		return host;

	}

	public int getPort() {

		return port;

	}

	public void setHost(InetAddress host) {

		this.host = host;

	}

	public void setPort(int port) {

		this.port = port;

	}

	@Override
	public boolean equals(Object obj) {

		Address o = (Address) obj;
		return host.toString().equals(o.getHost().toString()) && port == o.getPort();

	}

	@Override
	public int compareTo(Address o) {

		return equals(o) ? 0 : port - o.getPort();

	}

}
