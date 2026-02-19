import React, { useState, useEffect } from 'react';
import { collectionAPI, accountAPI } from '../api/api';
import { Plus, Eye, Search, Calendar, TrendingUp } from 'lucide-react';

const Collections = () => {
  const [collections, setCollections] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    accountId: '',
    collectedAmount: '',
    collectionDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    fetchCollections();
    fetchAccounts();
  }, []);

  const fetchCollections = async () => {
    try {
      const response = await collectionAPI.getAll();
      setCollections(Array.isArray(response) ? response : []);
    } catch (error) {
      console.error('Error fetching collections:', error);
      setCollections([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await accountAPI.getAll();
      setAccounts(Array.isArray(response) ? response : []);
    } catch (error) {
      console.error('Error fetching accounts:', error);
      setAccounts([]); // Set empty array on error
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await collectionAPI.create(formData);
      setShowModal(false);
      setFormData({ accountId: '', collectedAmount: '', collectionDate: new Date().toISOString().split('T')[0] });
      fetchCollections();
      fetchAccounts();
    } catch (error) {
      console.error('Error adding collection:', error);
    }
  };

  const filteredCollections = Array.isArray(collections) ? collections.filter(collection =>
    collection.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    collection.accountId.toString().includes(searchTerm) ||
    collection.collectionDate.includes(searchTerm)
  ) : [];

  const totalCollected = Array.isArray(collections) ? collections.reduce((sum, collection) => 
    sum + parseFloat(collection.collectedAmount), 0
  ).toFixed(2) : '0.00';

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
        <h1 className="text-3xl font-bold text-gray-900">Daily Collections</h1>
        <button
          onClick={() => setShowModal(true)}
          className="btn btn-primary flex items-center"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Collection
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="card">
          <div className="flex items-center">
            <div className="bg-green-500 p-3 rounded-full">
              <TrendingUp className="w-6 h-6 text-white" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Collections</p>
              <p className="text-2xl font-semibold text-gray-900">{collections.length}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <div className="bg-blue-500 p-3 rounded-full">
              <Calendar className="w-6 h-6 text-white" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Today's Date</p>
              <p className="text-lg font-semibold text-gray-900">
                {new Date().toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <div className="bg-purple-500 p-3 rounded-full">
              <TrendingUp className="w-6 h-6 text-white" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Collected</p>
              <p className="text-2xl font-semibold text-gray-900">₹{totalCollected}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="card mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search collections by customer name, account ID, or date..."
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
                <th className="table-header-cell">Collection ID</th>
                <th className="table-header-cell">Customer Name</th>
                <th className="table-header-cell">Account ID</th>
                <th className="table-header-cell">Collection Date</th>
                <th className="table-header-cell">Amount</th>
                <th className="table-header-cell">Month</th>
                <th className="table-header-cell">Year</th>
                <th className="table-header-cell">Actions</th>
              </tr>
            </thead>
            <tbody className="table-body">
              {filteredCollections.map((collection) => (
                <tr key={collection.collectionId}>
                  <td className="table-cell font-medium">#{collection.collectionId}</td>
                  <td className="table-cell">{collection.customerName}</td>
                  <td className="table-cell">{collection.accountId}</td>
                  <td className="table-cell">{new Date(collection.collectionDate).toLocaleDateString()}</td>
                  <td className="table-cell font-semibold text-green-600">₹{collection.collectedAmount}</td>
                  <td className="table-cell">{collection.month}</td>
                  <td className="table-cell">{collection.year}</td>
                  <td className="table-cell">
                    <button className="btn btn-secondary p-2">
                      <Eye className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {filteredCollections.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No collections found
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Add Daily Collection</h2>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Account *
                </label>
                <select
                  required
                  className="input-field"
                  value={formData.accountId}
                  onChange={(e) => setFormData({...formData, accountId: e.target.value})}
                >
                  <option value="">Select an account</option>
                  {accounts.filter(account => account.status === 'ACTIVE').map((account) => (
                    <option key={account.id} value={account.id}>
                      {account.accountNumber} - {account.customerName}
                    </option>
                  ))}
                </select>
              </div>
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Collected Amount *
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  step="0.01"
                  className="input-field"
                  value={formData.collectedAmount}
                  onChange={(e) => setFormData({...formData, collectedAmount: e.target.value})}
                />
              </div>
              
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Collection Date *
                </label>
                <input
                  type="date"
                  required
                  className="input-field"
                  value={formData.collectionDate}
                  onChange={(e) => setFormData({...formData, collectionDate: e.target.value})}
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
                  Add Collection
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Collections;
