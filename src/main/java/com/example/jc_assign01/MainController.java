package com.example.jc_assign01;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

public class MainController {

    // Menu Tab
    public AnchorPane anchorPane;
    public TabPane tabPane = new TabPane();
    public Tab menu;
    public MenuBar menuBar;
    public Menu file;
    public MenuItem openFile;
    public MenuItem clear;
    public Menu settings;
    public MenuItem reduceNoise;
    public MenuItem printArray;
    public MenuItem colourSets;
    public MenuItem circleSets;
    public TabPane tabPane2;
    public Tab firstImage;
    public Tab secondImage;
    public ImageView initImage;
    public ImageView alteredImage;
    public ListView<Object> imageInfo = new ListView<>();
    public TreeView<String> celestialObjectsInfo;
    public CheckMenuItem changeImage;

    public Slider luminanceSlider;
    public Slider noiseReductionValue;

    public Image image;
    public double luminanceValue;
    public int noiseValue;
    public Button printArr;
    public Button resetNoise;


    public int[] imageArray;
    public Hashtable<Integer, List<Integer>> disjointSets = new Hashtable<Integer, List<Integer>>();
    public ArrayList<Integer> roots = new ArrayList<>();
    private final Group circleGroup = new Group();
    private final Group objectIndexGroup = new Group();


    public int sumX = 0;
    public int sumY = 0;
    public boolean overlay = false;

    public void fileChooser() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        String fileName = "File Name: " + file.getName();
        String fileSize = String.format("File Size: " + file.getTotalSpace());
        String filePath = "File Path: " + file.getAbsolutePath();
        image = new Image(file.toURI().toString(), 512, 512, false, false);
        initImage.setImage(image);
        imageInfo.getItems().addAll(fileName, fileSize, filePath);
        luminanceValue = 0.5;
        convertImage();
    }

    public void resetImageView() {
        initImage.setImage(null);
        alteredImage.setImage(null);
        imageInfo.getItems().clear();
        celestialObjectsInfo.setRoot(null);
    }

    public void getLuminanceValue() {
        luminanceValue = luminanceSlider.getValue();
    }

    public void getNoiseValue() {
        noiseValue = (int) noiseReductionValue.getValue();
    }


    public static int find(int[] array, int index) {
        if (array[index] == -1) return -1;
        return array[index] == index ? index : (array[index] = find(array, array[index]));
    }

    public static void union(int[] a, int p, int q) {
        a[find(a, q)] = find(a, p); //The root of q is made reference the root of p
    }

    //1). The image is converted to black and white using a luminance value that is calculated on every pixel, if the value is <
    //    the user set value, the pixel will be set to black. An array is created by multiplying 512 x 512. This will create the
    //    same number of index's in the array as the amount of pixels in the image. Then if the brightness of the pixel is 0.0 (Black)
    //    the value at the index the pixel is set to -1. else the value is set to its own index. UnionPixels is then called.
    public void convertImage() {
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
                    if (brightness == 0) {
                        imageArray[y * width + x] = -1;
                    } else {
                        imageArray[y * width + x] = y * width + x;
                    }
                    Color newColor = Color.hsb(0, 0, brightness);
                    pixelWriter.setColor(x, y, newColor);
                    alteredImage.setImage(writableImage);
                }
            }
        unionPixels();
        showTreeView();
    }


    // 2).
    public void unionPixels() {
        int width = (int) image.getWidth();
        disjointSets.clear();

        int i = 0;
        while (i < imageArray.length - 1) {
            if (imageArray[i] != -1 && i % width != 0 && imageArray[i + 1] != -1) {
                union(imageArray, i, i + 1);
            }
            if (imageArray[i] != -1 && i / width != 511 && imageArray[i + width] != -1) {
                union(imageArray, i, i + width);
            }
            i++;
        }
        createDisjointSets();
    }

    public void createDisjointSets() {
        // Iterate through each pixel in the image array
        for (int i = 0; i < imageArray.length; i++) {
            // Find the root of the current pixel using the 'find' method
            int root = find(imageArray, i);
            // If a root is found, add the pixel to the list of pixels associated with that root
            if (root != -1) {
                // If the root is not already in the 'disjointSets' map, add it with an empty list
                if (!disjointSets.containsKey(root)) {
                    disjointSets.put(root, new ArrayList<Integer>());
                    roots.add(root);
                }
                // Add the current pixel to the list of pixels associated with the root
                disjointSets.get(root).add(i);
            }
        }
    }

    public void removeNoiseAndKeys() {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            Enumeration<Integer> keys = disjointSets.keys();
            while (keys.hasMoreElements()) {
                int key = keys.nextElement();
                List<Integer> pixelSet = disjointSets.get(key);
                int setSize = pixelSet.size();
                if (setSize < noiseValue || setSize > 500) {
                    for (int pixel : pixelSet) {
                        imageArray[pixel] = -1;
                    }
                    disjointSets.remove(key);
                }
            }
            for (int i = 0; i < imageArray.length; i++) {
                if (imageArray[i] == -1) {
                    int x = i % width;
                    int y = i / width;
                    Color newColor = Color.hsb(0, 0, 0.0);
                    pixelWriter.setColor(x, y, newColor);
                }
            }
            alteredImage.setImage(writableImage);
            showTreeView();
    }

    public void printArray() {
        if (changeImage.isSelected()) {
            for (int i = 0; i < imageArray.length; i++) {
                System.out.print(find(imageArray, i) + ((i + 1) % image.getWidth() == 0 ? "\n" : " "));
            }
        } else {
            System.out.println("Hi");
        }
    }

    public void colourDisjointSets() {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            Enumeration<Integer> keys = disjointSets.keys();
            while (keys.hasMoreElements()) {
                Color newColor = Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                int key = keys.nextElement();
                List<Integer> pixelSet = disjointSets.get(key);
                for (int pixel : pixelSet) {
                    int x = pixel % width;
                    int y = pixel / width;
                    pixelWriter.setColor(x, y, newColor);
                }
            }
            Color blackColor = Color.BLACK;
            for (int i = 0; i < imageArray.length; i++) {
                if (imageArray[i] == -1) {
                    int x = i % width;
                    int y = i / width;
                    pixelWriter.setColor(x, y, blackColor);
                }
            }
            image = writableImage;
            alteredImage.setImage(image);
            getSizeOfSets();
    }

    public void getSizeOfSets() {
        System.out.println("Total Objects Found: " + disjointSets.size());
        Enumeration<Integer> keys = disjointSets.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            List<Integer> pixelSet = disjointSets.get(key);
            System.out.println("Index: " + key + "   Pixel Size: " + pixelSet.size());
        }
    }

    public void circleObjects() {
        if (!overlay) {
            int index = 1;
            anchorPane.setOpacity(1);
            int width = (int) image.getWidth();
            Enumeration<Integer> keys = disjointSets.keys();
            while (keys.hasMoreElements()) {
                int key = keys.nextElement();
                List<Integer> pixelSet = disjointSets.get(key);
                int totalPixels = pixelSet.size();
                for (Integer integer : pixelSet) {
                    int x = integer % width;
                    int y = integer / width;
                    sumX += x;
                    sumY += y;
                }
                double centerX = (double) sumX / totalPixels;
                double centerY = (double) sumY / totalPixels;
                Circle circle = new Circle();
                Text text = new Text();
                text.setLayoutX(centerX);
                text.setLayoutY(centerY);
                text.setFill(Color.RED);
                text.setText(String.valueOf(index));
                circle.setCenterX(centerX);
                circle.setCenterY(centerY);
                circle.setFill(null);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(2);
                circle.setRadius(20);
                circleGroup.getChildren().addAll(circle);
                objectIndexGroup.getChildren().addAll(text);
                sumY = 0;
                sumX = 0;
                index++;
            }
            anchorPane.getChildren().addAll(circleGroup, objectIndexGroup);
            overlay = true;
        } else {
            anchorPane.getChildren().removeAll(circleGroup, objectIndexGroup);
            circleGroup.getChildren().clear();
            objectIndexGroup.getChildren().clear();
            overlay = false;
        }
    }

    public void showTreeView() {
        int index = 1;
        TreeItem<String> topItem = new TreeItem<>("Number of Celestial Objects: " + disjointSets.size());
        Enumeration<Integer> keys = disjointSets.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            List<Integer> pixelSet = disjointSets.get(key);
            int totalPixels = pixelSet.size();
            TreeItem<String> objects = new TreeItem<>("Celestial Object Number: " + index );
            topItem.getChildren().add(objects);
            TreeItem<String> objectInformation = new TreeItem<>(" Estimated Size (Pixel Units): " + totalPixels);
            objects.getChildren().add(objectInformation);
            index++;
        }

        celestialObjectsInfo.setRoot(topItem);
    }
}
