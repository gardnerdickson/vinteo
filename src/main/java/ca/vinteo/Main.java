package ca.vinteo;


import ca.vinteo.repository.*;
import ca.vinteo.ui.AddDirectoryWindow;
import ca.vinteo.ui.EventMediator;
import ca.vinteo.ui.MainWindow;
import ca.vinteo.ui.SettingsWindow;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.FileInfo;
import ca.vinteo.util.FileScanner;
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
import java.time.LocalDateTime;
import java.util.Comparator;
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

        // If there is no database file, this is the first time the application has been run
        boolean firstTimeSetup = Files.notExists(Paths.get(config.getSqliteFile()));
        DatabaseChangelogRepository changelogRepository = new DatabaseChangelogRepository(config.getSqliteFile());
        changelogRepository.checkAndExecuteUpdates(firstTimeSetup);

        ItemRepository itemRepository = new ItemRepository(config.getSqliteFile(), eventMediator);
        List<Item> items = itemRepository.findAllItems();
        new PlayHistoryRepository(config.getSqliteFile(), eventMediator);

        // If there are no items in the database, scan for items
        if (items.isEmpty()) {
            final LocalDateTime now = LocalDateTime.now();
            Set<FileInfo> filePaths = fileScanner.findAllFilePaths((num) -> null);
            items = filePaths.stream().map(entry -> new Item(null, entry.getPath(), entry.getName(), now)).collect(Collectors.toList());
            itemRepository.addItems(items);
        }

        new DesktopUtil(eventMediator);
        new VlcLauncher(config.getVlcCommand(), config.getTempDirectory(), eventMediator);
        List<String> resultItems = items.stream().sorted(Comparator.comparing(Item::getDateTimeAdded).reversed()).map(Item::getName).collect(Collectors.toList());
        MainWindow mainWindow = new MainWindow(primaryStage, eventMediator, FXCollections.observableArrayList(resultItems));
        mainWindow.setup();

        new SettingsWindow(eventMediator);
        new AddDirectoryWindow(eventMediator);

        primaryStage.show();
    }

}
