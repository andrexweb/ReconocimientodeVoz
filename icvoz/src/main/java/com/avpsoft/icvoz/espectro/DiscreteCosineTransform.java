/*
 *	Paquete				: espectro
 *	Archivo				: DiscreteCosineTransform.java
 *	Version                         : 1.0.0		2007-03-31
 *	Autor				: sphinx4-1.0beta
 *	URL					: http://cmusphinx.sourceforge.net/sphinx4
 *	Copyright			: Ver abajo.
 *	Version				: sphinx4-1.0beta-src
 *
 *	Este codigo es una implementacion modificada del paquete:
 *	edu.cmu.sphinx.frontend.transform.DiscreteCosineTransform.java
 *
 *	Todos los derechos reservados.  Usar bajo terminos de la licencia.
 * 
 *	Ver archivo "licencia.txt"(terceros) para informacion de como usar
 *	y distrubuir este archvio, y para garantizar las responsabilidades 
 *	adquiridas.
 */
 /*
 * Copyright 1999-2004 Carnegie Mellon University.  
 * Portions Copyright 2002-2004 Sun Microsystems, Inc.  
 * Portions Copyright 2002-2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package com.avpsoft.icvoz.espectro;

/**
 * Applies a logarithm and then a Discrete Cosine Transform (DCT) to the input
 * data. The input data is normally the mel spectrum. It has been proven that,
 * for a sequence of real numbers, the discrete cosine transform is equivalent
 * to the discrete Fourier transform. Therefore, this class corresponds to the
 * last stage of converting a signal to cepstra, defined as the inverse Fourier
 * transform of the logarithm of the Fourier transform of a signal. The property
 * {@link #PROP_CEPSTRUM_LENGTH}refers to the dimensionality of the coefficients
 * that are actually returned, defaulting to
 * {@link #PROP_CEPSTRUM_LENGTH_DEFAULT}. When the input is mel-spectrum, the
 * vector returned is the MFCC (Mel-Frequency Cepstral Coefficient) vector,
 * where the 0-th element is the energy value.
 */
public class DiscreteCosineTransform {

    private int cepstrumSize; // size of a Cepstrum
    private int numberMelFilters; // number of mel-filters
    private double[][] melcosine;

    public DiscreteCosineTransform(int numberMelFilters, int cepstrumSize) {
        this.numberMelFilters = numberMelFilters;
        this.cepstrumSize = cepstrumSize;
    }

    /**
     * Compute the MelCosine filter bank.
     */
    private void computeMelCosine() {
        melcosine = new double[cepstrumSize][numberMelFilters];
        double period = (double) 2 * numberMelFilters;
        for (int i = 0; i < cepstrumSize; i++) {
            double frequency = 2 * Math.PI * i / period;
            for (int j = 0; j < numberMelFilters; j++) {
                melcosine[i][j] = Math.cos(frequency * (j + 0.5));
            }
        }
    }

    /**
     * Process data, creating the mel cepstrum from an input spectrum frame.
     *
     * @param input a MelSpectrum frame
     *
     * @return a mel Cepstrum frame
     */
    public double[] process(double[] input)
            throws IllegalArgumentException {
        double[] melspectrum = (double[]) input.clone();

        if (melcosine == null) {
            numberMelFilters = melspectrum.length;
            computeMelCosine();

        } else if (melspectrum.length != numberMelFilters) {
            throw new IllegalArgumentException("MelSpectrum size is incorrect: melspectrum.length == "
                    + melspectrum.length + ", numberMelFilters == "
                    + numberMelFilters);
        }
        // first compute the log of the spectrum
        for (int i = 0; i < melspectrum.length; ++i) {
            if (melspectrum[i] > 0) {
                melspectrum[i] = Math.log(melspectrum[i]);
            } else {
                // in case melspectrum[i] isn't greater than 0
                // instead of trying to compute a log we just
                // assign a very small number
                melspectrum[i] = -1.0e+5;
            }
        }
        // create the cepstrum by apply the melcosine filter
        double[] cepstrum = applyMelCosine(melspectrum);
        /*DoubleData output = new DoubleData(cepstrum, input.getSampleRate(),
                input.getCollectTime(),
                input.getFirstSampleNumber());*/
        return cepstrum;
    }

    /**
     * Apply the MelCosine filter to the given melspectrum.
     *
     * @param melspectrum the MelSpectrum data
     *
     * @return MelCepstrum data produced by apply the MelCosine filter to the
     * MelSpectrum data
     */
    private double[] applyMelCosine(double[] melspectrum) {
        // create the cepstrum
        double[] cepstrum = new double[cepstrumSize];
        double period = (double) numberMelFilters;
        double beta = 0.5;
        // apply the melcosine filter
        for (int i = 0; i < cepstrum.length; i++) {
            if (numberMelFilters > 0) {
                double[] melcosine_i = melcosine[i];
                int j = 0;
                cepstrum[i] += (beta * melspectrum[j] * melcosine_i[j]);
                for (j = 1; j < numberMelFilters; j++) {
                    cepstrum[i] += (melspectrum[j] * melcosine_i[j]);
                }
                cepstrum[i] /= period;
            }
        }
        return cepstrum;
    }
}
