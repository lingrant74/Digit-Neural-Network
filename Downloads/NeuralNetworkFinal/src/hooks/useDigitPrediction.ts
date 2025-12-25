import { useState } from 'react';

interface PredictionResult {
  prediction: number;
  confidence?: number;
}

export const useDigitPrediction = (apiUrl: string = 'http://localhost:8080/predict') => {
  const [prediction, setPrediction] = useState<number | null>(null);
  const [confidence, setConfidence] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const predict = async (imageData: number[]) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ pixels: imageData }),
      });

      if (!response.ok) {
        throw new Error(`Server responded with ${response.status}`);
      }

      const result: PredictionResult = await response.json();
      setPrediction(result.prediction);
      setConfidence(result.confidence);
    } catch (err) {
      console.error('Prediction error:', err);
      setError(
        err instanceof Error 
          ? err.message 
          : 'Failed to connect to the prediction server'
      );
      setPrediction(null);
      setConfidence(undefined);
    } finally {
      setIsLoading(false);
    }
  };

  const reset = () => {
    setPrediction(null);
    setConfidence(undefined);
    setError(null);
  };

  return {
    prediction,
    confidence,
    isLoading,
    error,
    predict,
    reset,
  };
};
