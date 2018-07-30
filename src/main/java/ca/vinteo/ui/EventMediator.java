package ca.vinteo.ui;

import ca.vinteo.Finder;
import ca.vinteo.util.DesktopUtil;
import ca.vinteo.util.VlcLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EventMediator {

    private static final Logger logger = LoggerFactory.getLogger(EventMediator.class);

    private MainWindow mainWindow;
    private SettingsWindow settingsWindow;
    private Finder finder;
    private VlcLauncher vlcLauncher;
    private DesktopUtil desktopUtil;

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setSettingsWindow(SettingsWindow settingsWindow) {
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

    public void onSearchQueryChanged(String query) {
        List<String> results = finder.findLike(query);
        mainWindow.updateResultView(results);
    }

    public void onResultItemSelectionChanged() {
        logger.debug("Selected item changed");
    }

    public void onSettingsMenuItemClicked() {
        settingsWindow.start();
    }

    public void onExitMenuItemClicked() {
        mainWindow.close();
    }

    public void onResultItemDoubleClick(String selectedItem) {
        String filePathStr = finder.results().get(selectedItem);
        Path filePath = Paths.get(filePathStr);
        try {
            vlcLauncher.launch(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to play '" + filePath.toString() + "'", e);
        }
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
}
