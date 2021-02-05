package io.amrishraje.cblitetester;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdvanceSearchController {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceSearchController.class);
    public Button cancelButton;
    public TextArea searchArea;
    public TextField searchTextBox;
    public CheckBox matchWholeWords;
    public Button searchButton;
    public Label searchStatusLabel;
    MainController mainController;
    private FilteredList<Map.Entry<String, String>> filteredData;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void cancelSearch(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void searchDocuments(ActionEvent event) {
        filteredData = mainController.getFilteredData();
        String lowerCaseFilter = searchTextBox.getText();
        String[] searchList = lowerCaseFilter.split(";");
        AtomicInteger docCount = new AtomicInteger();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), x -> mainController.docCountAnchorPane.setVisible(false)));
        filteredData.setPredicate(tableData -> {
            if (containsWordsAhoCorasick(tableData.getValue(), searchList, matchWholeWords.isSelected())) {
                docCount.getAndIncrement();
                return true;
            } else return false;
        });
        mainController.docCountLabel.setText(docCount.toString() + " documents matched");
        mainController.docCountLabel.setVisible(true);
        mainController.docCountLabel.setStyle("-fx-background: rgba(30,30,30);\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-background-color: rgba(30,30,30,0.8);\n" +
                "    -fx-background-radius: 6px;\n" +
                "    -fx-background-insets: 0;\n" +
                "    -fx-padding: 0.667em 0.75em 0.667em 0.75em; /* 10px */\n" +
                "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.5) , 10, 0.0 , 0 , 3 );\n" +
                "    -fx-font-size: 0.85em;");
        mainController.docCountAnchorPane.setVisible(true);
        Platform.runLater(timeline::play);
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public static boolean containsWordsAhoCorasick(String inputString, String[] words, Boolean matchWholeWord) {
        Trie trie;
        if (matchWholeWord) {
            trie = Trie.builder().onlyWholeWords().ignoreCase().addKeywords(words).build();
        } else {
            trie = Trie.builder().ignoreCase().addKeywords(words).build();
        }
        Collection<Emit> emits = trie.parseText(inputString);
        boolean found = true;
        for(String word : words) {
            boolean contains = Arrays.toString(emits.toArray()).contains(word.toLowerCase());
            if (!contains) {
                found = false;
                break;
            }
        }
        return found;
    }
}
