import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface PredictionResultProps {
  prediction: number | null;
  confidence?: number;
  isLoading?: boolean;
  error?: string | null;
}

const PredictionResult: React.FC<PredictionResultProps> = ({
  prediction,
  confidence,
  isLoading,
  error,
}) => {
  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle className="text-center">Prediction Result</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center min-h-[150px]">
        {isLoading ? (
          <div className="flex flex-col items-center gap-3">
            <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin" />
            <p className="text-muted-foreground">Analyzing your digit...</p>
          </div>
        ) : error ? (
          <div className="text-center">
            <p className="text-destructive font-medium">{error}</p>
            <p className="text-sm text-muted-foreground mt-2">
              Make sure your Java API server is running
            </p>
          </div>
        ) : prediction !== null ? (
          <div className="flex flex-col items-center gap-4">
            <div className="text-8xl font-bold text-primary animate-in zoom-in duration-300">
              {prediction}
            </div>
            {confidence !== undefined && (
              <Badge variant="secondary" className="text-sm">
                Confidence: {confidence.toFixed(1)}%
              </Badge>
            )}
          </div>
        ) : (
          <p className="text-muted-foreground text-center">
            Draw a digit and click "Predict" to see the result
          </p>
        )}
      </CardContent>
    </Card>
  );
};

export default PredictionResult;
