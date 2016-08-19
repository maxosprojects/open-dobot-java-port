// File:         GenericRequest.java
// Created:      17.04.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.omirob.microservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement
public final class GenericRequest {

	private String username;

	private List<String> roles;

	private Map<String, String> params;

	public GenericRequest(String username, Map<String, String> params) {
		this.username = username;
		this.params = params;
	}

	public GenericRequest() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(final List<String> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "GenericRequest{" +
				"username='" + username + '\'' +
				", roles=" + roles +
				", params=" + params +
				'}';
	}
}
