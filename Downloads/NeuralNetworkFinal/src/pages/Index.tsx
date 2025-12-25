import { useState } from 'react';
import DrawingCanvas from '@/components/DrawingCanvas';
import PredictionResult from '@/components/PredictionResult';
import { useDigitPrediction } from '@/hooks/useDigitPrediction';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';

const Index = () => {
  const [apiUrl, setApiUrl] = useState('http://localhost:8080/predict');
  const { prediction, confidence, isLoading, error, predict } = useDigitPrediction(apiUrl);

  const handlePredict = (imageData: number[]) => {
    predict(imageData);
  };

  return (
    <div className="min-h-screen bg-background py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <header className="text-center mb-8">
          <h1 className="text-4xl font-bold text-foreground mb-2">
            Handwritten Digit Recognition
          </h1>
          <p className="text-muted-foreground text-lg">
            Draw a digit and let the neural network predict what you wrote
          </p>
        </header>

        <div className="flex flex-col lg:flex-row items-center justify-center gap-8">
          <DrawingCanvas onPredict={handlePredict} isLoading={isLoading} />
          <PredictionResult
            prediction={prediction}
            confidence={confidence}
            isLoading={isLoading}
            error={error}
          />
        </div>

        <Card className="mt-8 max-w-md mx-auto">
          <CardContent className="pt-6">
            <Label htmlFor="api-url" className="text-sm font-medium">
              API Endpoint URL
            </Label>
            <Input
              id="api-url"
              type="text"
              value={apiUrl}
              onChange={(e) => setApiUrl(e.target.value)}
              placeholder="http://localhost:8080/predict"
              className="mt-2"
            />
            <p className="text-xs text-muted-foreground mt-2">
              Your Java API should accept POST requests with JSON body: {`{ "pixels": [784 floats 0-1] }`}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Index;
