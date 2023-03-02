package com.example.jc_assign01;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;

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
    public Button printArr;

    public int[] imageArray;
    public ArrayList<Integer> sets = new ArrayList<>();

    public void fileChooser() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        String fileName = "File Name: " + file.getName();
        String fileSize = String.format("File Size: " + file.getTotalSpace());
        String filePath = "File Path: " + file.getAbsolutePath();
        image = new Image(file.toURI().toString(), 512, 512, false, false);
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

    public static int find(int[] array, int index) {
        if (array[index] == -1) return -1;
        return array[index] == index ? index : find(array, array[index]);
    }

    public static void unionBySize(int[] array, int p, int q) {
        int rootp = find(array, p);
        int rootq = find(array, q);

        int biggerRoot = array[rootp] < array[rootq] ? rootp : rootq;
        int smallerRoot = biggerRoot == rootp ? rootq : rootp;
        int smallSize = array[smallerRoot];

        array[smallerRoot] = biggerRoot;
        array[biggerRoot] += smallSize;
        //Value of merged root recalculated as the (negative)
        //total number of elements in the merged set
    }

    public void convertImage() {
        if (blackAndWhite.isSelected()) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            imageArray = new int[width * height];


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    double luminance = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                    double brightness = (luminance < luminanceValue) ? 0.0 : 1.0;
                    if (brightness == 0.0) {
                        imageArray[y * width + x] = -1;
                    } else {
                        imageArray[y * width + x] = y * width + x;
                    }
                    Color newColor = Color.hsb(0, 0, brightness);
                    pixelWriter.setColor(x, y, newColor);
                    initImage.setImage(writableImage);
                }
            }
        } else {
            initImage.setImage(image);
        }
        unionPixels();
    }

    public void unionPixels() {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int i = 0;
        while (i < width * height - 1) {
            if (imageArray[i] != -1 && imageArray[i + 1] != -1) {
                //imageArray[i +1] = imageArray[i];
                unionBySize(imageArray, imageArray[i], imageArray[i + 1]);
                if(imageArray[i] != -1 && imageArray[i + 512] != -1) {
                    unionBySize(imageArray, imageArray[i], imageArray[i + 512]);
                    //imageArray[i + 512] = imageArray[i];
                }
                if(imageArray[i] != -1 && imageArray[i + 511] != -1) {
                    unionBySize(imageArray, imageArray[i], imageArray[i + 512]);
                    //imageArray[i + 511] = imageArray[i];
                }
//                unionBySize(imageArray, imageArray[i], imageArray[i + 1]);
            }
            i++;
        }
    }

        public void printArray () {
            for (int i = 0; i < imageArray.length; i++) {
                System.out.print(find(imageArray, i) + ((i + 1) % image.getWidth() == 0 ? "\n" : " "));
            }
        }
    }