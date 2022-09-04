package foo;

import redis.clients.jedis.ConnectionFactory;

@Deprecated
public interface DeprecatedIntf {
	public void foo();
	public void bar(ConnectionFactory externalDep);
}
