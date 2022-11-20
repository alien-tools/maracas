package com.github.maracas.forges;

import java.util.List;

public interface ClientFetcher {
	List<Commit> fetchClients(Repository repository, Package pkg, int maxClients, int minStars);
}
