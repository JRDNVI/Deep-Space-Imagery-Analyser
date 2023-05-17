module com.example.jc_assign {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.jc_assign01 to javafx.fxml;
    exports com.example.jc_assign01;
}