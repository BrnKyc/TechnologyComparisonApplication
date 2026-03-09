
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TechnologyComparisonApp extends Application {

    private static final int TECH_COUNT = 3;
    private TextField[][] inputs = new TextField[TECH_COUNT][6];
    private TextField nField;                
    private RadioButton rbA, rbB;            // Varianten A/B
    private LineChart<Number, Number> chart;

    
    
    record Tech(double bearbMin, double ruestStd, double lohn, double matKg, double matPreis, double werkzeug) {
        double bearbKosten() {
        	return (bearbMin / 60.0) * lohn; 
        }
        double matKosten()   {
        	return matKg * matPreis;
        	}
        double varKosten()   {
        	return bearbKosten() + matKosten();
        	 }   
        double ruestKosten(int n, int losGroesse) {
            int nLose = (int) Math.ceil((double) n / losGroesse);
            return ruestStd * lohn * nLose;                            
        }
        double gesamtKosten(int n, int losGroesse) {
            return werkzeug + ruestKosten(n, losGroesse) + n * varKosten();
        }
    }

    
    
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(10));

        GridPane grid = createInputGrid();

        
        nField = new TextField("1000");
        nField.setPrefWidth(80);
        HBox nBox = new HBox(10, new Label("Max. Stückzahl n:"), nField);

      
        ToggleGroup tg = new ToggleGroup();
        rbA = new RadioButton("Variante A (1 Los x 1000)");
        rbA.setToggleGroup(tg); rbA.setSelected(true);
        rbB = new RadioButton("Variante B (5 Lose x 200)");
        rbB.setToggleGroup(tg);
        HBox varBox = new HBox(20, rbA, rbB);

        
        NumberAxis xAxis = new NumberAxis(); 
        xAxis.setLabel("Stückzahl n");
        NumberAxis yAxis = new NumberAxis(); 
        yAxis.setLabel("Kosten (€)");
        chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
    

        Button btn = new Button("aktualisieren ");
        btn.setOnAction(e -> updateChart());

        root.getChildren().addAll(grid, nBox, varBox, btn, chart);

     
        try { stage.getIcons().add(new Image("file:icon.png")); } catch (Exception ignored) {}
        stage.setTitle("Technologievergleich – Varianten A & B");
        stage.setScene(new Scene(root, 790, 650));
        updateChart();
        stage.show();
    }

    
    
    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(4);

        String[] rows = {
                "Bearbeitungszeit [min/Stk]", "Rüstzeit [h/Los]", "Lohn [€/h]",
                "Materialeinsatz [kg/Stk]", "Materialpreis [€/kg]", "Werkzeugkosten [€]"
        };
        double[][] defaults = {
                {24, 16, 9}, {4, 6, 8}, {18.5, 17.2, 15.9},
                {3.0, 2.8, 2.1}, {1.7, 1.9, 1.9}, {2500, 4300, 5500}
        };
        grid.add(new Label(""), 0, 0);
        grid.add(new Label("Technologie 1"), 1, 0);
        grid.add(new Label("Technologie 2"), 2, 0);
        grid.add(new Label("Technologie 3"), 3, 0);

        for (int r = 0; r < rows.length; r++) {
            grid.add(new Label(rows[r]), 0, r + 1);
            for (int t = 0; t < TECH_COUNT; t++) {
                TextField tf = new TextField(String.valueOf(defaults[r][t]));
                inputs[t][r] = tf;
                grid.add(tf, t + 1, r + 1);
            }
        }
        return grid;
    }

    
    private void updateChart() {
        chart.getData().clear();
        int maxN = parseInt(nField, 1000); if (maxN <= 0) maxN = 1000;
        int losGroesse = rbA.isSelected() ? 1000 : 200;
        String[] farben = {"blue", "green", "red"};

        for (int i = 0; i < TECH_COUNT; i++) {
            Tech tech = readTech(i);
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            s.setName("T" + (i + 1));
            for (int n = 0; n <= maxN; n += 20) {
                s.getData().add(new XYChart.Data<>(n, tech.gesamtKosten(n, losGroesse)));
            }
            chart.getData().add(s);
            int idx = i;
            s.nodeProperty().addListener((obs,o,n)->{ if(n!=null) n.setStyle("-fx-stroke:"+farben[idx]); });
            
        }
    }

    
    
    private Tech readTech(int idx) {
        return new Tech(
                parseDouble(inputs[idx][0]),
                parseDouble(inputs[idx][1]),
                parseDouble(inputs[idx][2]),
                parseDouble(inputs[idx][3]),
                parseDouble(inputs[idx][4]),
                parseDouble(inputs[idx][5]));
    }
    private double parseDouble(TextField tf){ try{ tf.setStyle(""); return Double.parseDouble(tf.getText().replace(',','.')); }catch(Exception e){ tf.setStyle("-fx-background-color: lightcoral"); return 0;} }
    private int parseInt(TextField tf,int fallback){ try{ tf.setStyle(""); return Integer.parseInt(tf.getText().trim()); }catch(Exception e){ tf.setStyle("-fx-background-color: lightcoral"); return fallback;} }

    public static void main(String[] args){launch(args);} }
