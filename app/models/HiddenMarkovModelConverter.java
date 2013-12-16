package models;

import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

import com.avaje.ebean.config.ScalarTypeConverter;

public class HiddenMarkovModelConverter implements ScalarTypeConverter<HiddenMarkovModel, String> {

  public HiddenMarkovModel getNullValue() {
      return null;
  }

  public String unwrapValue(HiddenMarkovModel beanType) {
    HmmWriter hmmWriter = new HmmWriter();
    Writer writer = new StringWriter();
    OpdfMultiGaussianMixtureWriter oPdfWriter = new OpdfMultiGaussianMixtureWriter();
    try {
      hmmWriter.write(writer, oPdfWriter, beanType.getHiddenMarkovModel());
    } catch (IOException e) {
      System.out.println("Oops ! An exception occured while saving the HMM.");
    }
    return writer.toString();
  }

  public HiddenMarkovModel wrapValue(String scalarType) {
    HmmReader hmmReader = new HmmReader();
    Reader reader = new StringReader(scalarType);
    OpdfMultiGaussianMixtureReader oPdfReader = new OpdfMultiGaussianMixtureReader();
    try {
      HiddenMarkovModel hiddenMarkovModel = new HiddenMarkovModel(hmmReader.read(reader, oPdfReader));
      return hiddenMarkovModel;
    } catch (Exception e) {
      System.out.println("Oops ! An exception occured while reading the HMM.");
    }
    return null;
  }
}