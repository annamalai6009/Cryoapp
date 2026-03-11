# FreezerGuard - Cryo Monitoring System

A real-time freezer monitoring and temperature tracking system built with React, TypeScript, and Vite.

## Features

- **Real-Time Monitoring**: Track temperature, door status, and power status in real-time
- **Smart Alerts**: Get notified immediately when temperatures exceed safe thresholds
- **Dashboard**: Comprehensive dashboard with freezer fleet overview
- **Export Data**: Export temperature history as CSV or PDF
- **User Management**: Admin and root access controls for system administration
- **WhatsApp Integration**: Receive alerts via WhatsApp

## Technologies

This project is built with:

- **Vite** - Fast build tool and dev server
- **TypeScript** - Type-safe JavaScript
- **React** - UI library
- **shadcn-ui** - Component library
- **Tailwind CSS** - Utility-first CSS framework
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Recharts** - Chart library for data visualization

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- npm or yarn

### Installation

1. Clone the repository:
```sh
git clone <YOUR_GIT_URL>
cd freezer-guardian-363fbdc6-main
```

2. Install dependencies:
```sh
npm install
```

3. Start the development server:
```sh
npm run dev
```

The application will be available at `http://localhost:8080`

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run build:dev` - Build for development
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm test` - Run tests
- `npm run test:watch` - Run tests in watch mode

## Project Structure

```
src/
├── components/      # Reusable React components
├── contexts/        # React context providers
├── pages/           # Page components
├── services/        # API service layer
└── main.tsx         # Application entry point
```

## Configuration

The API endpoint is configured in `src/services/apiClient.ts`. Update the `API_GATEWAY_URL` to point to your backend server.

## License

Private project - All rights reserved

