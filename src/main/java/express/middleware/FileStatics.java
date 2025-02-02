package express.middleware;

import express.filter.Filter;
import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileStatics implements HttpRequestHandler, Filter {

    private static final Logger log = LoggerFactory.getLogger(FileStatics.class);

    private String root;

    public FileStatics(String root) throws IOException {
        Path rootDir = Paths.get(root);

        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            throw new IOException(rootDir + " does not exists or isn't a directory.");
        }

        this.root = rootDir.toAbsolutePath().toString();
    }

    @Override
    public void handle(Request req, Response res) {
        try {
            String path = req.getURI().getPath();
            String contextCheck = req.getContext();
            if (path.indexOf(contextCheck) == 0)
                path = path.substring(contextCheck.length());
            if (path.length() <= 1)
                path = "index.html";
            Path reqFile = Paths.get(root + File.separator + path);
            if(reqFile.toFile().exists() && reqFile.toFile().isDirectory())
                reqFile = reqFile.resolve("index.html");
            if(reqFile.toFile().exists()) res.send(reqFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "file-statics";
    }

}
