package ca.vinteo;


import ca.vinteo.repository.InitializationRepository;
import ca.vinteo.repository.RepositoryException;
import ca.vinteo.ui.MainWindow;
import ca.vinteo.ui.MainWindowHandler;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public final class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String CONNECTION_STRING_PREFIX = "jdbc:sqlite:";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, RepositoryException {
        Parameters commandLineArguments = getParameters();
        if (commandLineArguments.getRaw().size() != 1) {
            throw new IllegalArgumentException("Expected exactly 1 parameter. Got " + commandLineArguments.getRaw().size());
        }
        Config config = new Config(Paths.get(commandLineArguments.getRaw().get(0)));

        // If the application has not been run before. Execute first time setup.
        if (Files.notExists(Paths.get(config.getSqliteFile()))) {
            logger.info("Performing first time setup...");
            InitializationRepository initializationRepository = new InitializationRepository(CONNECTION_STRING_PREFIX + config.getSqliteFile());
            initializationRepository.executeSetup();
        }

        Set<Path> directoryPaths = config.getDirectories().stream().map(dir -> Paths.get(dir)).collect(Collectors.toSet());
        Finder finder = new Finder(directoryPaths, config.getExtensions());
        setupUi(primaryStage, finder, config);
        primaryStage.show();
    }


    private void setupUi(Stage primaryStage, Finder finder, Config config) {
        MainWindowHandler mainWindowHandler = new MainWindowHandler(finder, config.getVlcCommand());
        MainWindow mainWindow = new MainWindow(primaryStage, mainWindowHandler);
        mainWindow.start(finder.results().keySet());
    }

}
