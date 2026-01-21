# Handwritten Digit Recognition — Frontend

This repository contains the React + TypeScript frontend for a handwritten digit recognition system. The application provides an interactive interface for drawing digits in the browser and displays real-time predictions returned from a neural network backend.

---

## Project Overview

The frontend allows users to draw handwritten digits on a canvas, preprocesses the input into a 28×28 normalized pixel grid, and sends the data to a backend REST API for inference. The predicted digit and confidence score are then displayed in the UI. The project emphasizes clean component structure, responsive design, and clear separation between frontend presentation and backend computation.

---

## How to Edit This Project

You can edit the project using any standard frontend development workflow.

### Use Your Preferred IDE (Recommended)

Clone the repository and work locally:

```sh
git clone <YOUR_GIT_URL>
cd <YOUR_PROJECT_NAME>
npm install
npm run dev
