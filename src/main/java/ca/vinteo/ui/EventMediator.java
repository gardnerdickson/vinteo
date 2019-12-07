package ca.vinteo.ui;

import ca.vinteo.FileScanner;
import ca.vinteo.repository.*;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.VlcLauncher;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EventMediator {

    private static final Logger logger = LoggerFactory.getLogger(EventMediator.class);

    private MainWindow mainWindow;
    private SettingsWindow settingsWindow;
    private AddDirectoryWindow addDirectoryWindow;
    private FileScanner fileScanner;
    private VlcLauncher vlcLauncher;
    private DesktopUtil desktopUtil;
    private UserSettingsRepository userSettingsRepository;
    private UserSettings userSettings;
    private ItemRepository itemRepository;
    private PlayHistoryRepository playHistoryRepository;

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setSettingsWindow(SettingsWindow settingsWindow) {
        this.settingsWindow = settingsWindow;
    }

    public void setFileScanner(FileScanner fileScanner) {
        this.fileScanner = fileScanner;
    }

    public void setVlcLauncher(VlcLauncher vlcLauncher) {
        this.vlcLauncher = vlcLauncher;
    }

    public void setDesktopUtil(DesktopUtil desktopUtil) {
        this.desktopUtil = desktopUtil;
    }

    public void setUserSettingsRepository(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public void setItemRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void setPlayHistoryRepository(PlayHistoryRepository playHistoryRepository) {
        this.playHistoryRepository = playHistoryRepository;
    }

    public void setAddDirectoryWindow(AddDirectoryWindow addDirectoryWindow) {
        this.addDirectoryWindow = addDirectoryWindow;
    }

    public void onSearchQueryChanged(String query) {
        try {
            List<Item> items = itemRepository.findUsingKeywords(query);
            List<String> itemNames = items.stream().map(Item::getName).collect(Collectors.toList());
            mainWindow.updateResultView(new ArrayList<>(itemNames));
            mainWindow.setStatusBarLabel("");
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void onResultItemSelectionChanged(String selectedItem) {
        if (selectedItem == null) {
            return;
        }
        try {
            Item item = itemRepository.findByName(selectedItem).orElseThrow(() -> new RuntimeException("Item not found: " + selectedItem));
            File file = new File(item.getPath());
            String directory = file.getParent();
            Optional<PlayHistoryItem> historyItem = playHistoryRepository.getMostRecentlyPlayedFileFromDirectory(directory);
            if (historyItem.isPresent()) {
                mainWindow.setStatusBarLabel("Most recently played file from same directory: " + historyItem.get().getName());
            } else {
                mainWindow.setStatusBarLabel("");
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Failed to retrieve most recently played file.", e);
        }
    }

    public void onSettingsMenuItemClicked() {
        try {
            userSettings = userSettingsRepository.load();
            settingsWindow.setDirectories(userSettings.getDirectories());
            settingsWindow.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load settings file.", e);
        }
    }

    public void onExitMenuItemClicked() {
        mainWindow.close();
    }

    public void onResultItemDoubleClick(String selectedItem) {
        launchItem(selectedItem);
    }

    public void onResultItemControlClick(String selectedItem) {
        try {
            Item item = itemRepository.findByName(selectedItem).orElseThrow(() -> new RuntimeException("Item not found: " + selectedItem));
            desktopUtil.openFolder(Paths.get(item.getPath()));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void onMainWindowPlayButtonPressed(String selectedItem) {
        launchItem(selectedItem);
    }

    public void onMainWindowOpenFolderButtonPressed(String selectedItem) {
        onResultItemControlClick(selectedItem);
    }

    public void onSettingsWindowRemoveDirectories(List<Integer> indices) {
        indices.forEach(index -> userSettings.getDirectories().remove(index.intValue()));
        try {
            userSettingsRepository.save(userSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsWindow.removeDirectories(indices);
    }

    public void onSettingsWindowAddDirectoryButtonClicked() {
        addDirectoryWindow.show();
    }

    public void onDirectoryDialogOkButtonClicked(String directory) {
        userSettings.getDirectories().add(directory);
        try {
            userSettingsRepository.save(userSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsWindow.setDirectories(userSettings.getDirectories());
        addDirectoryWindow.hide();
    }

    public void onDirectoryDialogCancelButtonClicked() {
        addDirectoryWindow.hide();
    }

    public void onMainWindowEnterKeyPressed(String selectedItem) {
        launchItem(selectedItem);
    }

    public void onMainWindowRescanButtonPressed() {
        Task<Void> rescanTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                logger.info("Clearing items.");
                final AtomicInteger count = new AtomicInteger(0);
                Map<String, String> results = fileScanner.findAllFilePaths((path) -> {
                    count.getAndIncrement();
                    updateMessage("Items scanned: " + count.get());
                    return null;
                });
                List<Item> items = results
                        .entrySet()
                        .stream()
                        .map(entry -> new Item(null, entry.getValue(), entry.getKey()))
                        .collect(Collectors.toList());
                logger.info("Adding items.");
                updateMessage("Adding " + items.size() + " items to database...");
                itemRepository.clearItems();
                itemRepository.addItems(items);
                logger.info("Done adding {} items", items.size());
                updateMessage("");
                return null;
            }
        };
        rescanTask.messageProperty().addListener((obs, oldMessage, newMessage) ->  {
            mainWindow.setStatusBarLabel(newMessage);
        });
        new Thread(rescanTask).start();
    }

    private void launchItem(String itemName) {
        try {
            Item item = itemRepository.findByName(itemName).orElseThrow(() -> new RuntimeException("Item not found: " + itemName));
            String filePathStr = item.getPath();
            Path filePath = Paths.get(filePathStr);
            vlcLauncher.launch(filePath);
            logItemToPlayHistory(item);
        } catch (IOException | RepositoryException e) {
            throw new RuntimeException("Failed to play file.", e);
        }
    }

    private void logItemToPlayHistory(Item item) {
        logger.info("Logging '{}' to play history.", item.getPath());
        try {
            playHistoryRepository.logItem(item);
        } catch (RepositoryException e) {
            throw new RuntimeException("Failed to log file to play history.", e);
        }
    }

}
