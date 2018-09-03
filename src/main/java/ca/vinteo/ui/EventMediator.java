package ca.vinteo.ui;

import ca.vinteo.Finder;
import ca.vinteo.repository.*;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.VlcLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventMediator {

    private static final Logger logger = LoggerFactory.getLogger(EventMediator.class);

    private MainWindow mainWindow;
    private SettingsWindow settingsWindow;
    private AddDirectoryWindow addDirectoryWindow;
    private Finder finder;
    private VlcLauncher vlcLauncher;
    private DesktopUtil desktopUtil;
    private UserSettingsRepository userSettingsRepository;
    private UserSettings userSettings;
    private ItemRepository itemRepository;

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setSettingsWindow(SettingsWindow settingsWindow) throws IOException {
        this.settingsWindow = settingsWindow;
    }

    public void setFinder(Finder finder) {
        this.finder = finder;
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

    public void setAddDirectoryWindow(AddDirectoryWindow addDirectoryWindow) {
        this.addDirectoryWindow = addDirectoryWindow;
    }


    public void onSearchQueryChanged(String query) {
        List<String> results = finder.findLike(query);
        mainWindow.updateResultView(results);
    }

    public void onResultItemSelectionChanged() {
        logger.debug("Selected item changed");
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
        desktopUtil.openFolder(Paths.get(finder.results().get(selectedItem)));
    }

    public void onPlayButtonPressed(String selectedItem) {
        onResultItemDoubleClick(selectedItem);
    }

    public void onOpenFolderButtonPressed(String selectedItem) {
        onResultItemControlClick(selectedItem);
    }

    public void onRemoveDirectories(List<Integer> indices) {
        indices.forEach(index -> userSettings.getDirectories().remove(index.intValue()));
        try {
            userSettingsRepository.save(userSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsWindow.removeDirectories(indices);
    }

    public void onAddDirectoryButtonClicked() {
        addDirectoryWindow.show();
    }

    public void onAddDirectoryOkButtonClicked(String directory) {
        userSettings.getDirectories().add(directory);
        try {
            userSettingsRepository.save(userSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsWindow.setDirectories(userSettings.getDirectories());
        addDirectoryWindow.hide();
    }

    public void onAddDirectoryCancelButtonClicked() {
        addDirectoryWindow.hide();
    }

    public void onMainWindowEnterKeyPressed(String selectedItem) {
        launchItem(selectedItem);
    }

    public void onMainWindowRescanButtonPressed() {
        try {
            logger.info("Clearing items.");
            itemRepository.clearItems();
            Map<String, String> results = finder.findAllFilePaths();
            List<Item> items = results
                    .entrySet()
                    .stream()
                    .map(entry -> new Item(null, entry.getValue(), entry.getKey()))
                    .collect(Collectors.toList());
            logger.info("Adding items.");
            itemRepository.addItems(items);
            logger.info("Done adding {} items", items.size());
        } catch (IOException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchItem(String item) {
        String filePathStr = finder.results().get(item);
        Path filePath = Paths.get(filePathStr);
        try {
            vlcLauncher.launch(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to play '" + filePath.toString() + "'", e);
        }
    }
}
