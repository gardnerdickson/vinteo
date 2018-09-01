package ca.vinteo;


import ca.vinteo.repository.InitializationRepository;
import ca.vinteo.repository.RepositoryException;
import ca.vinteo.repository.UserSettings;
import ca.vinteo.repository.UserSettingsRepository;
import ca.vinteo.ui.AddDirectoryWindow;
import ca.vinteo.ui.EventMediator;
import ca.vinteo.ui.MainWindow;
import ca.vinteo.ui.SettingsWindow;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.VlcLauncher;
import javafx.application.Application;
import javafx.collections.FXCollections;
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
        ApplicationConfiguration config;
        if (commandLineArguments.getRaw().size() == 0) {
            config = new ApplicationConfiguration();
        } else if (commandLineArguments.getRaw().size() == 1) {
            config = new ApplicationConfiguration(Paths.get(commandLineArguments.getRaw().get(0)));
        } else {
            throw new IllegalArgumentException("Expected 0 or 1 parameters. Got " + commandLineArguments.getRaw().size());
        }

        // If the application has not been run before. Execute first time setup.
        if (Files.notExists(Paths.get(config.getSqliteFile()))) {
            logger.info("Performing first time setup...");
            InitializationRepository initializationRepository = new InitializationRepository(CONNECTION_STRING_PREFIX + config.getSqliteFile());
            initializationRepository.executeSetup();
        }

        EventMediator eventMediator = new EventMediator();

        UserSettingsRepository userSettingsRepo = new UserSettingsRepository(Paths.get(config.getUserSettingsFile()), eventMediator);
        UserSettings userSettings = userSettingsRepo.load();

        Set<Path> directoryPaths = userSettings.getDirectories().stream().map(dir -> Paths.get(dir)).collect(Collectors.toSet());
        Finder finder = new Finder(directoryPaths, userSettings.getFileExtensions(), eventMediator);

        new DesktopUtil(eventMediator);
        new VlcLauncher(config.getVlcCommand(), eventMediator);

        MainWindow mainWindow = new MainWindow(primaryStage, eventMediator, FXCollections.observableArrayList(finder.results().keySet()));
        mainWindow.setup();

        new SettingsWindow(eventMediator);
        new AddDirectoryWindow(eventMediator);

        primaryStage.show();
    }

}
