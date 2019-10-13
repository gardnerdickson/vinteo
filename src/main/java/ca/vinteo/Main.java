package ca.vinteo;


import ca.vinteo.repository.*;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Caught unhandled exception.", e);
        }
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

        EventMediator eventMediator = new EventMediator();

        UserSettingsRepository userSettingsRepo = new UserSettingsRepository(Paths.get(config.getUserSettingsFile()), eventMediator);
        UserSettings userSettings = userSettingsRepo.load();

        Set<Path> directoryPaths = userSettings.getDirectories().stream().map(dir -> Paths.get(dir)).collect(Collectors.toSet());
        FileScanner fileScanner = new FileScanner(directoryPaths, userSettings.getFileExtensions(), eventMediator);

        // If there is not database file, create it.
        if (Files.notExists(Paths.get(config.getSqliteFile()))) {
            logger.info("Performing first time setup...");
            InitializationRepository initializationRepository = new InitializationRepository(config.getSqliteFile());
            initializationRepository.executeSetup();
        }

        ItemRepository itemRepository = new ItemRepository(config.getSqliteFile(), eventMediator);
        List<Item> items = itemRepository.findAllItems();

        // If there are no items in the database, scan for items
        if (items.isEmpty()) {
            Map<String, String> filePaths = fileScanner.findAllFilePaths();
            items = filePaths.entrySet().stream().map(entry -> new Item(null, entry.getValue(), entry.getKey())).collect(Collectors.toList());
            itemRepository.addItems(items);
        }

        new DesktopUtil(eventMediator);
        new VlcLauncher(config.getVlcCommand(), eventMediator);
        List<String> resultItems = items.stream().map(Item::getName).collect(Collectors.toList());
        MainWindow mainWindow = new MainWindow(primaryStage, eventMediator, FXCollections.observableArrayList(resultItems));
        mainWindow.setup();

        new SettingsWindow(eventMediator);
        new AddDirectoryWindow(eventMediator);

        primaryStage.show();
    }

}
