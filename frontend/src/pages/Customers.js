import React, { useState, useEffect } from 'react';
import { customerAPI } from '../api/api';
import { Plus, Edit, Eye, Search } from 'lucide-react';

const Customers = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(5);
  const [totalCustomers, setTotalCustomers] = useState(0);
  const [formData, setFormData] = useState({
    name: '',
    mobileNumber: '',
    email: '',
    address: '',
    accountType: 'THREE_YEARS',
  });
  const [isEditing, setIsEditing] = useState(false);
  const [editingCustomerId, setEditingCustomerId] = useState(null);
  const [showClosed, setShowClosed] = useState(false);

  useEffect(() => {
    if (showClosed) {
      fetchClosedCustomers();
    } else {
      fetchCustomers();
    }
  }, [currentPage, itemsPerPage, searchTerm, showClosed]);

  const fetchCustomers = async () => {
    try {
      const response = await customerAPI.getAll();
      const allCustomers = Array.isArray(response) ? response : [];
      
      // Filter only ACTIVE customers
      const filtered = allCustomers.filter(customer =>
        customer.status === 'ACTIVE' && (
          customer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          customer.mobileNumber.includes(searchTerm)
        )
      );
      
      // Calculate pagination
      const total = filtered.length;
      const startIndex = (currentPage - 1) * itemsPerPage;
      const endIndex = startIndex + itemsPerPage;
      const paginatedCustomers = filtered.slice(startIndex, endIndex);
      
      setCustomers(paginatedCustomers);
      setTotalCustomers(total);
    } catch (error) {
      console.error('Error fetching customers:', error);
      setCustomers([]);
      setTotalCustomers(0);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isEditing) {
        await customerAPI.update(editingCustomerId, formData);
      } else {
        await customerAPI.create(formData);
      }
      setShowModal(false);
      setIsEditing(false);
      setEditingCustomerId(null);
      setFormData({ name: '', mobileNumber: '', email: '', address: '', accountType: 'THREE_YEARS' });
      fetchCustomers();
    } catch (error) {
      console.error(`Error ${isEditing ? 'updating' : 'creating'} customer:`, error);
    }
  };

  const handleSearch = (term) => {
    setSearchTerm(term);
    setCurrentPage(1); // Reset to first page when searching
  };

  const fetchClosedCustomers = async () => {
    try {
      const response = await customerAPI.getAllIncludingClosed();
      const allCustomers = Array.isArray(response) ? response : [];
      
      // Filter only CLOSED customers
      const filtered = allCustomers.filter(customer =>
        customer.status === 'CLOSED' && (
          customer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          customer.mobileNumber.includes(searchTerm)
        )
      );
      
      // Calculate pagination
      const total = filtered.length;
      const startIndex = (currentPage - 1) * itemsPerPage;
      const endIndex = startIndex + itemsPerPage;
      const paginatedCustomers = filtered.slice(startIndex, endIndex);
      
      setCustomers(paginatedCustomers);
      setTotalCustomers(total);
    } catch (error) {
      console.error('Error fetching closed customers:', error);
      setCustomers([]);
      setTotalCustomers(0);
    } finally {
      setLoading(false);
    }
  };

  const handleActivateCustomer = async (customerId, customerName) => {
    const confirmed = window.confirm(`Are you sure you want to activate customer "${customerName}"?`);
    if (!confirmed) return;
    
    try {
      await customerAPI.updateCustomerStatus(customerId, 'ACTIVE');
      if (showClosed) {
        fetchClosedCustomers(); // Refresh closed list
      } else {
        fetchCustomers(); // Refresh active list
      }
      alert('Customer activated successfully!');
    } catch (error) {
      console.error('Error activating customer:', error);
      alert('Failed to activate customer');
    }
  };

  const handleDeactivateCustomer = async (customerId, customerName) => {
    const confirmed = window.confirm(`Are you sure you want to deactivate customer "${customerName}"?`);
    if (!confirmed) return;
    
    try {
      await customerAPI.updateCustomerStatus(customerId, 'CLOSED');
      fetchCustomers(); // Refresh active list
      alert('Customer deactivated successfully!');
    } catch (error) {
      console.error('Error deactivating customer:', error);
      alert('Failed to deactivate customer');
    }
  };

  const totalPages = Math.ceil(totalCustomers / itemsPerPage);

  const handleViewCustomer = (customer) => {
    // View customer details - could open a modal or navigate to details page
    alert(`Viewing customer: ${customer.name}\nMobile: ${customer.mobileNumber}\nEmail: ${customer.email}\nAddress: ${customer.address}`);
  };

  const handleEditCustomer = (customer) => {
    // Edit customer - populate form with customer data
    setFormData({
      name: customer.name,
      mobileNumber: customer.mobileNumber,
      email: customer.email || '',
      address: customer.address || '',
      accountType: customer.accountType
    });
    setEditingCustomerId(customer.id);
    setIsEditing(true);
    setShowModal(true);
  };

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
        <h1 className="text-3xl font-bold text-gray-900">Customers</h1>
        <button
          onClick={() => setShowModal(true)}
          className="btn btn-primary flex items-center"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Customer
        </button>
      </div>

      <div className="card mb-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search customers by name or mobile number..."
              className="input-field pl-10"
              value={searchTerm}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>
          <div className="flex items-center gap-2">
            <label className="text-sm font-medium text-gray-700">Show:</label>
            <select
              value={itemsPerPage}
              onChange={(e) => setItemsPerPage(Number(e.target.value))}
              className="input-field w-20"
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
            <span className="text-sm text-gray-600">per page</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowClosed(!showClosed)}
              className={`btn px-3 py-1 ${
                showClosed ? 'bg-red-600 text-white' : 'bg-green-600 text-white'
              }`}
            >
              {showClosed ? 'Show Active' : 'Show Closed'}
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="table-container">
          <table className="table">
            <thead className="table-header">
              <tr>
                <th className="table-header-cell">Name</th>
                <th className="table-header-cell">Mobile Number</th>
                <th className="table-header-cell">Address</th>
                <th className="table-header-cell">Account Type</th>
                <th className="table-header-cell">Status</th>
                <th className="table-header-cell">Actions</th>
              </tr>
            </thead>
            <tbody className="table-body">
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td className="table-cell font-medium">{customer.name}</td>
                  <td className="table-cell">{customer.mobileNumber}</td>
                  <td className="table-cell">{customer.address || '-'}</td>
                  <td className="table-cell">{customer.accountType.replace('_', ' ')}</td>
                  <td className="table-cell">
                    <span className={`status-badge ${
                      customer.status === 'ACTIVE' ? 'status-active' : 'status-inactive'
                    }`}>
                      {customer.status}
                    </span>
                  </td>
                  <td className="table-cell">
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => handleViewCustomer(customer)}
                        className="btn btn-secondary p-2"
                        title="View Customer Details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleEditCustomer(customer)}
                        className="btn btn-secondary p-2"
                        title="Edit Customer"
                        style={{ display: customer.status === 'CLOSED' ? 'none' : 'block' }}
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      {showClosed && customer.status === 'CLOSED' && (
                        <button 
                          onClick={() => handleActivateCustomer(customer.id, customer.name)}
                          className="btn btn-green p-2"
                          title="Activate Customer"
                        >
                          <Plus className="w-4 h-4" />
                        </button>
                      )}
                      {!showClosed && customer.status === 'ACTIVE' && (
                        <button 
                          onClick={() => handleDeactivateCustomer(customer.id, customer.name)}
                          className="btn btn-red p-2"
                          title="Deactivate Customer"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {customers.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No customers found
            </div>
          )}
        </div>
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="card mt-6">
          <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
            <div className="text-sm text-gray-600">
              Showing {((currentPage - 1) * itemsPerPage) + 1} to {Math.min(currentPage * itemsPerPage, totalCustomers)} of {totalCustomers} customers
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="btn btn-secondary px-3 py-1 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let pageNum;
                  if (totalPages <= 5) {
                    pageNum = i + 1;
                  } else if (currentPage <= 3) {
                    pageNum = i + 1;
                  } else if (currentPage >= totalPages - 2) {
                    pageNum = totalPages - 4 + i;
                  } else {
                    pageNum = currentPage - 2 + i;
                  }
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => setCurrentPage(pageNum)}
                      className={`px-3 py-1 rounded ${
                        currentPage === pageNum
                          ? 'bg-primary-600 text-white'
                          : 'btn btn-secondary'
                      }`}
                    >
                      {pageNum}
                    </button>
                  );
                })}
              </div>
              
              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
                className="btn btn-secondary px-3 py-1 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">{isEditing ? 'Edit Customer' : 'Add New Customer'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Name *
                </label>
                <input
                  type="text"
                  required
                  className="input-field"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                />
              </div>
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Mobile Number *
                </label>
                <input
                  type="text"
                  required
                  maxLength={10}
                  className="input-field"
                  value={formData.mobileNumber}
                  onChange={(e) => setFormData({...formData, mobileNumber: e.target.value})}
                />
              </div>
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Address
                </label>
                <textarea
                  className="input-field"
                  rows={3}
                  value={formData.address}
                  onChange={(e) => setFormData({...formData, address: e.target.value})}
                />
              </div>
              
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Account Type *
                </label>
                <select
                  required
                  className="input-field"
                  value={formData.accountType}
                  onChange={(e) => setFormData({...formData, accountType: e.target.value})}
                >
                  <option value="THREE_YEARS">3 Years</option>
                  <option value="FIVE_YEARS">5 Years</option>
                </select>
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
                  Save Customer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Customers;
