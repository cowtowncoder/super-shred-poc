package com.cowtowncoder.supershred;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.datastax.astra.sdk.AstraClient;
import com.datastax.astra.sdk.databases.domain.Database;
import com.datastax.oss.driver.api.core.CqlSession;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Shredder implements Callable<Integer>
{
	@Option(names = {"-t", "--token"}, required=true,
			description = "Astra Token (starts with 'AstraCS:')")
	private String astraToken;

	@Option(names = {"-i", "--db-id"}, required=true,
			description = "Database Id (UUID)")
	private String dbId;

	@Option(names = {"-r", "--db-region"}, required=false,
			description = "Database region (like 'us-east1'")
	private String dbRegion = "us-east1";

	@Option(names = {"-k", "--keyspace"}, required=false,
			description = "Database region (like 'ks'")
	private String keyspace = "default";

	@Override
	public Integer call() throws Exception {
		try (AstraClient astraClient = configureAstraClient()) {
			Optional<Database> dbMaybe = astraClient.apiDevopsDatabases().databaseByName("supershred").find();
			if (!dbMaybe.isPresent()) {
				System.err.println("No Database 'supershred' found!");
				return 1;
			}
			final Database db = dbMaybe.get();
		    System.out.println("databaseId=" + db.getId());
		    System.out.println("databaseRegion=" + db.getInfo().getRegion());
		    System.out.println("keyspace=" + db.getInfo().getKeyspace());

		    testCqlApi(astraClient);
		}
		return 0;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new Shredder()).execute(args);
        System.exit(exitCode);
	}

	private AstraClient configureAstraClient() {
		return AstraClient.builder()
				.withToken(astraToken)
				.withDatabaseId(dbId)
				.withDatabaseRegion(dbRegion)
				.enableCql()
				.build();
	}

	public static void testCqlApi(AstraClient astraClient) {
		  System.out.println("\n[STARGATE/CQL]");
		  CqlSession cqlSession = astraClient.cqlSession();
		  System.out.println("+ Cql Version (cql)   : " + cqlSession
		    .execute("SELECT cql_version from system.local")
		    .one().getString("cql_version"));
		}
}
