package com.example.jc_assign01;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {

    // Menu Tab
    public TabPane tabPane = new TabPane();
    public Tab menu;
    public MenuBar menuBar;
    public Menu file;
    public MenuItem openFile;
    public MenuItem clear;
    public ImageView initImage;
    public ListView<Object> imageInfo = new ListView<>();
    public CheckBox blackAndWhite;
    public Slider luminanceSlider;

    public Image image;
    public double luminanceValue;

    public void fileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(null);
        String fileName = "File Name: " + file.getName();
        String fileSize = String.format("File Size: " + file.getTotalSpace());
        String filePath = "File Path: " + file.getAbsolutePath();
        image = new Image(file.getAbsolutePath());
        initImage.setImage(image);
        imageInfo.getItems().addAll(fileName, fileSize, filePath);
    }

    public void resetImageView() {
        initImage.setImage(null);
        imageInfo.getItems().clear();
    }

    public void getLuminanceValue() {
        luminanceValue = luminanceSlider.getValue();
    }

    public void convertImage() {
        if (blackAndWhite.isSelected()) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = pixelReader.getColor(x, y);
                    double luminance = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                    double brightness = (luminance < luminanceValue) ? 0.0 : 1.0;
                    Color newColor = Color.hsb(0, 0, brightness);
                    pixelWriter.setColor(x, y, newColor);
                    initImage.setImage(writableImage);
                }
            }
        }
        else {
            initImage.setImage(image);
        }
    }
}