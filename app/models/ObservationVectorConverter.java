package models;

import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.StreamTokenizer;
import java.io.IOException;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

import com.avaje.ebean.config.ScalarTypeConverter;

public class ObservationVectorConverter implements ScalarTypeConverter<ObservationVector, String> {

  public ObservationVector getNullValue() {
      return null;
  }

  public String unwrapValue(ObservationVector beanType) {
    ObservationVectorWriter observationVectorWriter = new ObservationVectorWriter();
    Writer writer = new StringWriter();
    try {
      observationVectorWriter.write(beanType, writer);
    } catch (IOException e) {
      System.out.println("Oops ! An exception occured while saving the ObservationVector.");
    }
    return writer.toString();
  }

  public ObservationVector wrapValue(String scalarType) {
    ObservationVectorReader observationVectorReader = new ObservationVectorReader();
    Reader reader = new StringReader(scalarType);
    try {
      ObservationVector observationVector = observationVectorReader.read(new StreamTokenizer(reader));
      return observationVector;
    } catch (Exception e) {
      System.out.println("Oops ! An exception occured while reading the ObservationVector.");
    }
    return null;
  }
}