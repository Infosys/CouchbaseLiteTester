package io.amrishraje.cblitetester;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
        filteredData.setPredicate(tableData -> {
            return containsWordsAhoCorasick(tableData.getValue(), searchList, matchWholeWords.isSelected());
        });
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
