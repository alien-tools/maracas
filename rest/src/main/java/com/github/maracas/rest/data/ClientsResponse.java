package com.github.maracas.rest.data;

import com.github.maracas.forges.github.Client;
import com.github.maracas.forges.github.Package;

import java.util.Collections;
import java.util.List;

public record ClientsResponse(
	String message,
	String owner,
	String name,
	List<Package> packages,
	List<Client> clients
) {
	public ClientsResponse(String message) {
		this(message, "", "", Collections.emptyList(), Collections.emptyList());
	}

	public ClientsResponse(String owner, String name, List<Package> packages, List<Client> clients) {
		this("ok", owner, name, packages, clients);
	}
}
