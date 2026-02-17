# POCAS Frontend

A React-based frontend for the POCAS (Daily Collection Management System).

## Features

- **Dashboard**: Overview with key statistics and metrics
- **Customer Management**: Add, view, and search customers
- **Account Management**: Create and manage customer accounts
- **Daily Collections**: Record and track daily collections
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Built with Tailwind CSS for a clean, professional look

## Tech Stack

- **React 18**: Modern React with hooks
- **React Router**: Client-side routing
- **Tailwind CSS**: Utility-first CSS framework
- **Axios**: HTTP client for API calls
- **Lucide React**: Beautiful icons

## Prerequisites

- Node.js 14+ and npm
- Backend server running on `http://localhost:8080`

## Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The application will open in your browser at `http://localhost:3000`.

## Configuration

The API base URL is configured in `src/api/api.js`. By default, it connects to `http://localhost:8080/api`.

To change the backend URL, you can:
1. Set the `REACT_APP_API_URL` environment variable
2. Or modify the `API_BASE_URL` constant in `src/api/api.js`

## Project Structure

```
src/
├── api/           # API service layer
├── components/    # Reusable components
├── pages/         # Page components
├── App.js         # Main app component
├── index.js       # Entry point
└── index.css      # Global styles
```

## Available Scripts

- `npm start` - Runs the app in development mode
- `npm run build` - Builds the app for production
- `npm test` - Launches the test runner
- `npm run eject` - Ejects from Create React App (one-way operation)

## API Integration

The frontend integrates with the following backend APIs:

### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer

### Accounts
- `GET /api/accounts` - Get all accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create new account

### Daily Collections
- `GET /api/daily-collections/all` - Get all collections
- `GET /api/daily-collections/account/{accountId}` - Get collections by account
- `GET /api/daily-collections/customer/{customerId}` - Get collections by customer
- `POST /api/daily-collections` - Add new collection

## Features in Detail

### Dashboard
- Total customers count
- Total accounts count
- Total collections count
- Total amount collected
- Recent collections overview
- Account status summary

### Customer Management
- Add new customers with validation
- View all customers in a table
- Search customers by name or mobile number
- View customer details and status

### Account Management
- Create new accounts for customers
- View all accounts with details
- Search accounts by account number or customer name
- Account status tracking (Active, Completed, Inactive)

### Daily Collections
- Add daily collections for accounts
- View all collections with details
- Search collections by customer, account, or date
- Collection statistics and summaries

## Styling

The application uses Tailwind CSS with custom utility classes for consistent styling:

- `.card` - Standard card component styling
- `.btn` - Base button styling
- `.btn-primary`, `.btn-secondary`, `.btn-success`, `.btn-danger` - Button variants
- `.input-field` - Form input styling
- `.table` - Table styling
- `.status-badge` - Status indicator styling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is part of the POCAS system.
