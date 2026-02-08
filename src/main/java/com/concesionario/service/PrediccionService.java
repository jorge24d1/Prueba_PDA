package com.concesionario.service;

import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.InputStream;

@Service
public class PrediccionService {

    private Classifier modelo;
    private Instances estructura;

    public PrediccionService() {
        try {
            System.out.println(" Cargando modelo y estructura...");

            // Cargar modelo
            InputStream modeloStream = getClass().getClassLoader().getResourceAsStream("modelo_PA.model");
            if (modeloStream == null) {
                throw new RuntimeException(" No se encontró modelo_PA.model");
            }
            modelo = (Classifier) weka.core.SerializationHelper.read(modeloStream);

            // Cargar estructura desde el ARFF
            InputStream estructuraStream = getClass().getClassLoader().getResourceAsStream("estructura.arff");
            if (estructuraStream == null) {
                throw new RuntimeException(" No se encontró estructura.arff");
            }

            DataSource source = new DataSource(estructuraStream);
            estructura = source.getDataSet();
            estructura.setClassIndex(estructura.numAttributes() - 1);

            System.out.println("✅ Modelo cargado: " + modelo.getClass().getSimpleName());
            System.out.println("✅ Estructura cargada: " + estructura.numAttributes() + " atributos");
            System.out.println("✅ Clase: " + estructura.classAttribute().name());

        } catch (Exception e) {
            System.err.println(" Error cargando modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String predecir(double citas, double antiguedad, String estado, String interes, double tiempo) {
        try {
            Instance nuevaInstancia = new weka.core.DenseInstance(estructura.numAttributes());
            nuevaInstancia.setDataset(estructura);

            nuevaInstancia.setValue(0, citas);
            nuevaInstancia.setValue(1, antiguedad);
            nuevaInstancia.setValue(2, estado);
            nuevaInstancia.setValue(3, interes);
            nuevaInstancia.setValue(4, tiempo);

            double prediccion = modelo.classifyInstance(nuevaInstancia);
            return estructura.classAttribute().value((int) prediccion);

        } catch (Exception e) {
            System.err.println("❌ Error en predicción: " + e.getMessage());
            return "Error";
        }
    }


    public double obtenerProbabilidadSi(double citas, double antiguedad, String estado, String interes, double tiempo) {
        try {
            Instance nuevaInstancia = new weka.core.DenseInstance(estructura.numAttributes());
            nuevaInstancia.setDataset(estructura);

            nuevaInstancia.setValue(0, citas);
            nuevaInstancia.setValue(1, antiguedad);
            nuevaInstancia.setValue(2, estado);
            nuevaInstancia.setValue(3, interes);
            nuevaInstancia.setValue(4, tiempo);

            double[] distribucion = modelo.distributionForInstance(nuevaInstancia);
            return distribucion[0] * 100;

        } catch (Exception e) {
            return 50.0;
        }
    }
}