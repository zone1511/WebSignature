package models;

import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.StreamTokenizer;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

import com.avaje.ebean.config.ScalarTypeConverter;

public class SamplesConverter implements ScalarTypeConverter<Samples, String> {

  public Samples getNullValue() {
      return null;
  }

  public String unwrapValue(Samples beanType) {
    ObservationSequencesWriter observationSequenceWriter = new ObservationSequencesWriter();
    ObservationVectorWriter observationVectorWriter = new ObservationVectorWriter();
    Writer writer = new StringWriter();
    try {
      List<ObservationVector> seq = beanType.toObservationVectorList();
      List<List<ObservationVector>> fSeq = new ArrayList<List<ObservationVector>>();
      fSeq.add(seq);
      observationSequenceWriter.write(writer, observationVectorWriter, fSeq);
    } catch (IOException e) {
      System.out.println("Oops ! An exception occured while saving the Samples.");
    }
    return writer.toString();
  }

  public Samples wrapValue(String scalarType) {
    ObservationSequencesReader observationSequenceReader = new ObservationSequencesReader();
    ObservationVectorReader observationVectorReader = new ObservationVectorReader();
    Reader reader = new StringReader(scalarType);
    try {
      List<ObservationVector> observationVectorSequence = 
        observationSequenceReader.readSequence(observationVectorReader, reader);
      return new Samples(observationVectorSequence, observationVectorSequence.get(0));
    } catch (Exception e) {
      System.out.println("Oops ! An exception occured while reading the Samples.");
    }
    return null;
  }
}
