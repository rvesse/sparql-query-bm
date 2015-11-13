package net.sf.sparql.benchmarking.loader.query;

import java.io.File;
import java.io.IOException;

import net.sf.sparql.benchmarking.loader.AbstractOperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.operations.Operation;

public abstract class AbstractQueryOperationLoader extends AbstractOperationLoader {

    public AbstractQueryOperationLoader() {
        super();
    }

    @Override
    public Operation load(File baseDir, String[] args) throws IOException {
        if (args.length < 1)
            throw new IOException("Insufficient arguments to load a query operation");

        String queryFile = args[0];
        String name = queryFile;
        if (args.length > 1) {
            name = args[1];
        }

        String query = readFile(baseDir, queryFile);
        return createQueryOperation(name, query);
    }

    /**
     * Creates the operation
     * 
     * @param name
     *            Name
     * @param query
     *            Query string
     * @return Operation
     */
    protected abstract Operation createQueryOperation(String name, String query);

    @Override
    public OperationLoaderArgument[] getArguments() {
        OperationLoaderArgument[] args = new OperationLoaderArgument[2];
        args[0] = new OperationLoaderArgument("Query File", "Provides a file that contains the SPARQL query to be run.",
                OperationLoaderArgument.TYPE_FILE);
        args[1] = AbstractOperationLoader.getNameArgument(true);
        return args;
    }

}