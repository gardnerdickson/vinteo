package ca.vinteo.ui;

import ca.vinteo.util.FileInfo;
import ca.vinteo.util.FileScanner;
import ca.vinteo.repository.*;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.Vlc;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class EventMediator {

    private static final Logger logger = LoggerFactory.getLogger(EventMediator.class);

    private MainWindow mainWindow;
    private SettingsWindow settingsWindow;
    private AddDirectoryWindow addDirectoryWindow;
    private FileScanner fileScanner;
    private Vlc vlc;
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

    public void setVlcLauncher(Vlc vlc) {
        this.vlc = vlc;
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
        Set<Path> paths = userSettings.getDirectories().stream().map(Paths::get).collect(Collectors.toSet());
        fileScanner.setDirectories(paths);
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

                BiFunction<Set<FileInfo>, Set<String>, Void> removeItems = (filesScanned, existingPaths) -> {
                    Set<String> pathsScanned = filesScanned.stream().map(FileInfo::getPath).collect(Collectors.toSet());
                    Set<String> removedPaths = existingPaths
                            .stream()
                            .filter(path -> !pathsScanned.contains(path))
                            .collect(Collectors.toSet());
                    if (!removedPaths.isEmpty()) {
                        logger.info("Removing items.");
                        updateMessage("[LABEL]Removing " + removedPaths.size() + " items from database...");
                        try {
                            itemRepository.removeItems(removedPaths);
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                        logger.info("Done removing {} items.", removedPaths.size());
                    }
                    return null;
                };

                BiFunction<Set<FileInfo>, Set<String>, Void> addItems = (filesScanned, existingPaths) -> {
                    Set<Item> newItems = filesScanned
                            .stream()
                            .filter(file -> !existingPaths.contains(file.getPath()))
                            .map(file -> new Item(null, file.getPath(), file.getName(), null))
                            .collect(Collectors.toSet());
                    logger.info("Adding items.");
                    updateMessage("[LABEL]Adding " + newItems.size() + " items to database...");
                    try {
                        itemRepository.addItems(newItems);
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                    logger.info("Done adding {} items", newItems.size());
                    return null;
                };

                final AtomicInteger count = new AtomicInteger(0);
                Set<FileInfo> filesScanned = fileScanner.findAllFilePaths((path) -> {
                    count.getAndIncrement();
                    updateMessage("[LABEL]Items scanned: " + count.get());
                    return null;
                });
                Set<String> existingPaths = itemRepository.findAllItems().stream().map(Item::getPath).collect(Collectors.toSet());

                removeItems.apply(filesScanned, existingPaths);
                addItems.apply(filesScanned, existingPaths);
                updateMessage("[UPDATE_RESULT_VIEW]");

                return null;
            }
        };
        rescanTask.messageProperty().addListener((obs, oldMessage, newMessage) ->  {
            if (newMessage.startsWith("[LABEL]")) {
                mainWindow.setStatusBarLabel(newMessage.replace("[LABEL]", ""));
            } else if (newMessage.startsWith("[UPDATE_RESULT_VIEW]")) {
                // Find all items again so we can sort by date added.
                try {
                    List<String> sortedItemNames = itemRepository
                            .findAllItems()
                            .stream()
                            .sorted(Comparator.comparing(Item::getDateTimeAdded).reversed())
                            .map(Item::getName)
                            .collect(Collectors.toList());
                    mainWindow.setStatusBarLabel("");
                    mainWindow.updateResultView(sortedItemNames);
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        new Thread(rescanTask).start();
    }

    private void launchItem(String itemName) {
        try {
            Item item = itemRepository.findByName(itemName).orElseThrow(() -> new RuntimeException("Item not found: " + itemName));
            String filePathStr = item.getPath();
            Path filePath = Paths.get(filePathStr);
            vlc.launch(filePath);
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
