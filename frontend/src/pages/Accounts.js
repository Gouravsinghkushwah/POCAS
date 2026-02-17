import React, { useState, useEffect } from 'react';
import { accountAPI, customerAPI } from '../api/api';
import { Plus, Eye, Search, Calendar } from 'lucide-react';

const Accounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    customerId: '',
    accountType: 'THREE_YEARS',
    monthlyKist: '',
    startDate: '',
  });

  useEffect(() => {
    fetchAccounts();
    fetchCustomers();
  }, []);

  const fetchAccounts = async () => {
    try {
      const response = await accountAPI.getAll();
      setAccounts(Array.isArray(response) ? response : []);
    } catch (error) {
      console.error('Error fetching accounts:', error);
      setAccounts([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomers = async () => {
    try {
      const response = await customerAPI.getAll();
      setCustomers(Array.isArray(response) ? response : []);
    } catch (error) {
      console.error('Error fetching customers:', error);
      setCustomers([]); // Set empty array on error
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await accountAPI.create(formData);
      setShowModal(false);
      setFormData({ customerId: '', accountType: 'THREE_YEARS', monthlyKist: '', startDate: '' });
      fetchAccounts();
    } catch (error) {
      console.error('Error creating account:', error);
    }
  };

  const filteredAccounts = Array.isArray(accounts) ? accounts.filter(account =>
    account.accountNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    account.customerName.toLowerCase().includes(searchTerm.toLowerCase())
  ) : [];

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Accounts</h1>
        <button
          onClick={() => setShowModal(true)}
          className="btn btn-primary flex items-center"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Account
        </button>
      </div>

      <div className="card mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search accounts by account number or customer name..."
            className="input-field pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="card">
        <div className="table-container">
          <table className="table">
            <thead className="table-header">
              <tr>
                <th className="table-header-cell">Account Number</th>
                <th className="table-header-cell">Customer Name</th>
                <th className="table-header-cell">Account Type</th>
                <th className="table-header-cell">Monthly Kist</th>
                <th className="table-header-cell">Total Months</th>
                <th className="table-header-cell">Remaining Months</th>
                <th className="table-header-cell">Status</th>
                <th className="table-header-cell">Actions</th>
              </tr>
            </thead>
            <tbody className="table-body">
              {filteredAccounts.map((account) => (
                <tr key={account.id}>
                  <td className="table-cell font-medium">{account.accountNumber}</td>
                  <td className="table-cell">{account.customerName}</td>
                  <td className="table-cell">{account.accountType.replace('_', ' ')}</td>
                  <td className="table-cell">₹{account.monthlyKist}</td>
                  <td className="table-cell">{account.totalMonths}</td>
                  <td className="table-cell">{account.remainingMonths}</td>
                  <td className="table-cell">
                    <span className={`status-badge ${
                      account.status === 'ACTIVE' ? 'status-active' : 
                      account.status === 'COMPLETED' ? 'status-completed' : 'status-inactive'
                    }`}>
                      {account.status}
                    </span>
                  </td>
                  <td className="table-cell">
                    <button className="btn btn-secondary p-2">
                      <Eye className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredAccounts.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No accounts found
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Add New Account</h2>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Customer *
                </label>
                <select
                  required
                  className="input-field"
                  value={formData.customerId}
                  onChange={(e) => setFormData({...formData, customerId: e.target.value})}
                >
                  <option value="">Select a customer</option>
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name} - {customer.mobileNumber}
                    </option>
                  ))}
                </select>
              </div>
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Account Type *
                </label>
                <select
                  required
                  className="input-field"
                  value={formData.accountType}
                  onChange={(e) => setFormData({...formData, accountType: e.target.value})}
                >
                  <option value="THREE_YEARS">3 Years (36 months)</option>
                  <option value="FIVE_YEARS">5 Years (60 months)</option>
                </select>
              </div>
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Monthly Kist Amount *
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  step="0.01"
                  className="input-field"
                  value={formData.monthlyKist}
                  onChange={(e) => setFormData({...formData, monthlyKist: e.target.value})}
                />
              </div>
              
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Start Date *
                </label>
                <input
                  type="date"
                  required
                  className="input-field"
                  value={formData.startDate}
                  onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                />
              </div>
              
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Create Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Accounts;
