package com.github.maracas.experiments;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClientsCSVManager extends CSVManager {
	public ClientsCSVManager(String path) throws IOException {
		super(path);
	}

	@Override
	protected String[] buildColumns() {
		return new String[] {"cursor", "owner", "name", "stars", "groupId",
			"artifactId", "version", "path", "clients", "relevantClients",
			"cowner", "cname", "cstars"};
	}

	@Override
	protected Map<String, String> buildColumnsFormat() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("cursor", "%s");
		map.put("owner", "%s");
		map.put("name", "%s");
		map.put("url", "%s");
		map.put("stars", "%d");
		map.put("groupId", "%s");
		map.put("artifactId", "%s");
		map.put("version", "%s");
		map.put("path", "%s");
		map.put("clients", "%d");
		map.put("relevantClients", "%d");
		map.put("cowner", "%s");
		map.put("cname", "%s");
		map.put("curl", "%s");
		map.put("cstars", "%d");
		return map;
	}

	@Override
	public void writeRecord(Object obj) {
		// TODO Auto-generated method stub

	}
}
