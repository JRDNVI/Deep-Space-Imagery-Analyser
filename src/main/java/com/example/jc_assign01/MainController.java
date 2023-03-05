package com.example.jc_assign01;

import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

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
    public Hashtable<Integer, List<Integer>> disjointSets = new Hashtable<Integer, List<Integer>>();
    public ArrayList<Integer> roots = new ArrayList<>();
    public Text numOfPlants;


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
        return array[index] == index ? index : (array[index] = find(array, array[index]));
    }

    public static void union(int[] a, int p, int q) {
        a[find(a, q)] = find(a, p); //The root of q is made reference the root of p
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
                    if (brightness == 0) {
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

    }

    public void createDisjointSets() {
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
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

        Enumeration<Integer> node = disjointSets.keys();
        while (node.hasMoreElements()) {
            int key = node.nextElement();
            int setSize = disjointSets.get(key).size();
            if (setSize < 7) {
                for (int pixel : disjointSets.get(key)) {
                    imageArray[pixel] = -1;
                    int x = pixel % width;
                    int y = pixel / width;
                    Color newColor = Color.hsb(0, 0, 0.0);
                    pixelWriter.setColor(x, y, newColor);

                }
            }
        }
        initImage.setImage(writableImage);


        //imageArray[disjointSets.get(key).get(0)] = -1;
        // Print the resulting disjoint sets
        // System.out.println(disjointSets);
        // System.out.println(roots.toString());
        numOfPlants.setText(String.valueOf((disjointSets.size())));

        // colourPlants();
    }

    public void colourPlants() {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();


        List<Integer> temp = disjointSets.get(find(imageArray, 34327));

    }

    public void printArray() {
        createDisjointSets();
        for (int i = 0; i < imageArray.length; i++) {
            System.out.print(find(imageArray, i) + ((i + 1) % image.getWidth() == 0 ? "\n" : " "));
        }
    }
}