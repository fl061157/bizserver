package com.handwin.config;

public final class SystemConfigBean {
	
	private String ipStrategyUrl;
    
	private String ipStrategyUdpPath;
    
	private String udpIpDefault;
    
	private String udpPortDefaul;
    
	private String udpNodeDefault;
    
	private String udpP2pDefault;

    private String ipStrategyTcpPath;

    private String traversingServerPath;
	
	
	public String getIpStrategyUdpPath() {
		return ipStrategyUdpPath;
	}
	
	public String getIpStrategyUrl() {
		return ipStrategyUrl;
	}
	
	public String getUdpIpDefault() {
		return udpIpDefault;
	}
	
	public String getUdpNodeDefault() {
		return udpNodeDefault;
	}
	
	public String getUdpP2pDefault() {
		return udpP2pDefault;
	}
	
	public String getUdpPortDefaul() {
		return udpPortDefaul;
	}
	
	public void setIpStrategyUdpPath(String ipStrategyUdpPath) {
		this.ipStrategyUdpPath = ipStrategyUdpPath;
	}
	
	public void setIpStrategyUrl(String ipStrategyUrl) {
		this.ipStrategyUrl = ipStrategyUrl;
	}
	
	public void setUdpIpDefault(String udpIpDefault) {
		this.udpIpDefault = udpIpDefault;
	}
	
	public void setUdpNodeDefault(String udpNodeDefault) {
		this.udpNodeDefault = udpNodeDefault;
	}
	
	public void setUdpP2pDefault(String udpP2pDefault) {
		this.udpP2pDefault = udpP2pDefault;
	}
	
	public void setUdpPortDefaul(String udpPortDefaul) {
		this.udpPortDefaul = udpPortDefaul;
	}

    public String getIpStrategyTcpPath() {
        return ipStrategyTcpPath;
    }

    public void setIpStrategyTcpPath(String ipStrategyTcpPath) {
        this.ipStrategyTcpPath = ipStrategyTcpPath;
    }

    public String getTraversingServerPath() {
        return traversingServerPath;
    }

    public void setTraversingServerPath(String traversingServerPath) {
        this.traversingServerPath = traversingServerPath;
    }
}
