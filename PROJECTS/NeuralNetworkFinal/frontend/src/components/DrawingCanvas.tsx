import React, { useRef, useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Eraser, Send, RotateCcw } from 'lucide-react';

interface DrawingCanvasProps {
  onPredict: (imageData: number[]) => void;
  isLoading?: boolean;
}

const DrawingCanvas: React.FC<DrawingCanvasProps> = ({ onPredict, isLoading }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hasDrawn, setHasDrawn] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    // Set up canvas with white background
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 20;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
  }, []);

  const getCoordinates = (e: React.MouseEvent | React.TouchEvent) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };

    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;

    if ('touches' in e) {
      return {
        x: (e.touches[0].clientX - rect.left) * scaleX,
        y: (e.touches[0].clientY - rect.top) * scaleY,
      };
    }
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY,
    };
  };

  const startDrawing = (e: React.MouseEvent | React.TouchEvent) => {
    e.preventDefault();
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx) return;

    const { x, y } = getCoordinates(e);
    ctx.beginPath();
    ctx.moveTo(x, y);
    setIsDrawing(true);
    setHasDrawn(true);
  };

  const draw = (e: React.MouseEvent | React.TouchEvent) => {
    e.preventDefault();
    if (!isDrawing) return;

    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx) return;

    const { x, y } = getCoordinates(e);
    ctx.lineTo(x, y);
    ctx.stroke();
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const clearCanvas = () => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx || !canvas) return;

    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    setHasDrawn(false);
  };

  const getImageData = (): number[] => {
    const canvas = canvasRef.current;
    if (!canvas) return [];

    // Create a temporary canvas to resize to 28x28
    const tempCanvas = document.createElement('canvas');
    tempCanvas.width = 28;
    tempCanvas.height = 28;
    const tempCtx = tempCanvas.getContext('2d');
    if (!tempCtx) return [];

    // Draw the original canvas scaled down to 28x28
    tempCtx.drawImage(canvas, 0, 0, 28, 28);

    // Get the pixel data
    const imageData = tempCtx.getImageData(0, 0, 28, 28);
    const pixels: number[] = [];

    // Convert to grayscale values (0-1) - MNIST format
    for (let i = 0; i < imageData.data.length; i += 4) {
      // Use the red channel (since we're drawing white on black)
      const grayscale = imageData.data[i] / 255;
      pixels.push(grayscale);
    }

    return pixels;
  };

  const handlePredict = () => {
    const imageData = getImageData();
    onPredict(imageData);
  };

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle className="text-center">Draw a Digit (0-9)</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center gap-4">
        <div className="border-4 border-primary rounded-lg overflow-hidden touch-none">
          <canvas
            ref={canvasRef}
            width={280}
            height={280}
            className="cursor-crosshair"
            onMouseDown={startDrawing}
            onMouseMove={draw}
            onMouseUp={stopDrawing}
            onMouseLeave={stopDrawing}
            onTouchStart={startDrawing}
            onTouchMove={draw}
            onTouchEnd={stopDrawing}
          />
        </div>
        
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={clearCanvas}
            className="gap-2"
          >
            <RotateCcw className="w-4 h-4" />
            Clear
          </Button>
          
          <Button
            onClick={handlePredict}
            disabled={!hasDrawn || isLoading}
            className="gap-2"
          >
            <Send className="w-4 h-4" />
            {isLoading ? 'Predicting...' : 'Predict'}
          </Button>
        </div>
        
        <p className="text-sm text-muted-foreground text-center">
          Draw a single digit using your mouse or finger
        </p>
      </CardContent>
    </Card>
  );
};

export default DrawingCanvas;
