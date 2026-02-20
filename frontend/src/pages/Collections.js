import React, { useState, useEffect } from 'react';
import { collectionAPI, accountAPI } from '../api/api';
import { Plus, Eye, Search, Calendar, TrendingUp, X, CheckCircle, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';

const Collections = () => {
  const [collections, setCollections] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showPaymentStatusModal, setShowPaymentStatusModal] = useState(false);
  const [showColorPicker, setShowColorPicker] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [paymentStatus, setPaymentStatus] = useState([]);
  const [paymentStatusLoading, setPaymentStatusLoading] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const [paidColor, setPaidColor] = useState(() => {
    return localStorage.getItem('paidColor') || 'bg-green-600 border-green-700 text-white';
  });
  const [unpaidColor, setUnpaidColor] = useState(() => {
    return localStorage.getItem('unpaidColor') || 'bg-orange-200 border-orange-300 text-gray-800';
  });
  const [formData, setFormData] = useState({
    accountId: '',
    collectedAmount: '',
    collectionDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    fetchCollections();
    fetchAccounts();
  }, []);

  useEffect(() => {
    fetchCollections();
  }, [currentPage, recordsPerPage, searchTerm]);

  useEffect(() => {
    localStorage.setItem('paidColor', paidColor);
  }, [paidColor]);

  useEffect(() => {
    localStorage.setItem('unpaidColor', unpaidColor);
  }, [unpaidColor]);

  const fetchCollections = async () => {
    try {
      const response = await collectionAPI.getAllPaginated(currentPage, recordsPerPage, searchTerm);
      setCollections(Array.isArray(response.content) ? response.content : []);
      setTotalRecords(response.totalElements || 0);
    } catch (error) {
      console.error('Error fetching collections:', error);
      setCollections([]);
      setTotalRecords(0);
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

  const fetchPaymentStatus = async (accountId) => {
    try {
      setPaymentStatusLoading(true);
      const response = await collectionAPI.getPaymentStatus(accountId);
      console.log('Full API response:', response); // Debug log
      
      // Handle different response structures
      let paymentData = response;
      if (response.data && Array.isArray(response.data)) {
        paymentData = response.data;
      } else if (response.data && response.data.data && Array.isArray(response.data.data)) {
        paymentData = response.data.data;
      }
      
      console.log('Payment status data extracted:', paymentData); // Debug log
      setPaymentStatus(Array.isArray(paymentData) ? paymentData : []);
      
      // Also fetch monthly summary to get daily amount
      const currentDate = new Date();
      const month = currentDate.getMonth() + 1;
      const year = currentDate.getFullYear();
      
      try {
        const monthlySummary = await collectionAPI.getMonthlyPaymentSummary(accountId, month, year);
        console.log('Monthly summary:', monthlySummary);
        // Store daily amount for calculations
        if (monthlySummary && monthlySummary.expectedAmount) {
          // Calculate daily amount from expected amount and days passed
          const dailyAmount = monthlySummary.expectedAmount / monthlySummary.totalDays;
          // Store it for use in calendar
          window.currentDailyAmount = dailyAmount;
        }
      } catch (error) {
        console.log('Could not fetch monthly summary, using default daily amount');
        window.currentDailyAmount = 200; // Default fallback
      }
      
    } catch (error) {
      console.error('Error fetching payment status:', error);
      setPaymentStatus([]);
    } finally {
      setPaymentStatusLoading(false);
    }
  };

  const handleViewPaymentStatus = (collection) => {
    setSelectedAccount(collection);
    setShowPaymentStatusModal(true);
    fetchPaymentStatus(collection.accountId);
  };

  // Calendar helper functions
  const getDaysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  const formatCurrency = (amount) => {
    return `₹${amount.toFixed(2)}`;
  };

  const getPaymentStatusForDate = (date) => {
    // Format date to YYYY-MM-DD format to match backend response
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;
    
    const status = paymentStatus.find(s => s.date === dateStr);
    console.log('Looking for date:', dateStr, 'Status found:', status); // Debug log
    return status || { paid: false, paidAmount: 0 };
  };

  // Calculate monthly summary
  const getMonthlySummary = () => {
    const today = new Date();
    const daysInMonth = getDaysInMonth(currentMonth);
    const currentDate = new Date();
    const daysPassed = Math.min(today.getDate(), daysInMonth);
    
    // Count paid and unpaid days up to today
    let paidDays = 0;
    let unpaidDays = 0;
    let totalPaidAmount = 0;
    let dailyAmount = window.currentDailyAmount || 200; // Use dynamic daily amount
    
    for (let day = 1; day <= daysPassed; day++) {
      const date = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
      const status = getPaymentStatusForDate(date);
      
      if (status.paid) {
        paidDays++;
        totalPaidAmount += status.paidAmount;
      } else {
        unpaidDays++;
      }
    }
    
    const expectedAmount = daysPassed * dailyAmount;
    const remainingAmount = expectedAmount - totalPaidAmount;
    
    return {
      paidDays,
      unpaidDays,
      expectedAmount,
      totalPaidAmount,
      remainingAmount,
      daysPassed
    };
  };

  const navigateMonth = (direction) => {
    setCurrentMonth(prev => {
      const newDate = new Date(prev);
      if (direction === 'prev') {
        newDate.setMonth(newDate.getMonth() - 1);
      } else {
        newDate.setMonth(newDate.getMonth() + 1);
      }
      return newDate;
    });
  };

  const renderCalendar = () => {
    const daysInMonth = getDaysInMonth(currentMonth);
    const firstDay = getFirstDayOfMonth(currentMonth);
    const days = [];
    
    // Add empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="p-2 border border-gray-100"></div>);
    }
    
    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const currentDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
      const paymentInfo = getPaymentStatusForDate(currentDate);
      const isToday = currentDate.toDateString() === new Date().toDateString();
      
      days.push(
        <div
          key={day}
          className={`p-2 border-2 min-h-[80px] ${
            currentDate <= new Date() 
              ? paymentInfo.paid 
                ? paidColor
                : unpaidColor
              : 'bg-white border-gray-200 text-gray-800'
          } ${
            isToday ? 'ring-4 ring-blue-500 ring-opacity-50' : ''
          }`}
        >
          <div className="text-xs font-bold mb-1">{day}</div>
          {currentDate <= new Date() ? (
            paymentInfo.paid ? (
              <div className="text-xs">
                <div className="flex items-center mb-1">
                  <CheckCircle className="w-3 h-3 mr-1" />
                  <span className="font-semibold">Paid</span>
                </div>
                <div className="font-bold text-sm">
                  {formatCurrency(paymentInfo.paidAmount)}
                </div>
              </div>
            ) : (
              <div className="text-xs">
                <div className="flex items-center mb-1">
                  <AlertCircle className="w-3 h-3 mr-1" />
                  <span className="font-semibold">Unpaid</span>
                </div>
                <div className="font-bold text-sm">
                  {formatCurrency(0)}
                </div>
              </div>
            )
          ) : (
            <div className="text-xs text-gray-400">
              <div className="text-center mt-3">
                {/* Empty for future dates */}
              </div>
            </div>
          )}
        </div>
      );
    }
    
    return days;
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
        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowColorPicker(true)}
            className="btn btn-secondary flex items-center"
          >
            <Calendar className="w-4 h-4 mr-2" />
            Color Settings
          </button>
          <button
            onClick={() => setShowModal(true)}
            className="btn btn-primary flex items-center"
          >
            <Plus className="w-4 h-4 mr-2" />
            Add Collection
          </button>
        </div>
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
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search collections by customer name, account ID, or date..."
              className="input-field pl-10"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
            />
          </div>
          <div className="flex items-center gap-2">
            <label className="text-sm font-medium text-gray-700">Show:</label>
            <select
              value={recordsPerPage}
              onChange={(e) => {
                setRecordsPerPage(Number(e.target.value));
                setCurrentPage(1);
              }}
              className="input-field w-20"
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
            <span className="text-sm text-gray-600">records</span>
          </div>
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
              {collections.map((collection) => (
                <tr key={collection.collectionId}>
                  <td className="table-cell font-medium">#{collection.collectionId}</td>
                  <td className="table-cell">{collection.customerName}</td>
                  <td className="table-cell">{collection.accountId}</td>
                  <td className="table-cell">{new Date(collection.collectionDate).toLocaleDateString()}</td>
                  <td className="table-cell font-semibold text-green-600">₹{collection.collectedAmount}</td>
                  <td className="table-cell">{collection.month}</td>
                  <td className="table-cell">{collection.year}</td>
                  <td className="table-cell">
                    <button 
                      onClick={() => handleViewPaymentStatus(collection)}
                      className="btn btn-secondary p-2"
                      title="View Payment Status"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {collections.length === 0 && !loading && (
            <div className="text-center py-8 text-gray-500">
              No collections found
            </div>
          )}
        </div>
      </div>

      {/* Pagination Controls */}
      {totalRecords > 0 && (
        <div className="card mt-6">
          <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
            <div className="text-sm text-gray-600">
              Showing {((currentPage - 1) * recordsPerPage) + 1} to {Math.min(currentPage * recordsPerPage, totalRecords)} of {totalRecords} records
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
                {Array.from({ length: Math.min(5, Math.ceil(totalRecords / recordsPerPage)) }, (_, i) => {
                  const totalPages = Math.ceil(totalRecords / recordsPerPage);
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
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, Math.ceil(totalRecords / recordsPerPage)))}
                disabled={currentPage === Math.ceil(totalRecords / recordsPerPage)}
                className="btn btn-secondary px-3 py-1 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
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

      {/* Payment Status Modal */}
      {showPaymentStatusModal && selectedAccount && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold">
                Payment Status - Account #{selectedAccount.accountId}
              </h2>
              <button
                onClick={() => setShowPaymentStatusModal(false)}
                className="btn btn-secondary p-2"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mb-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="font-medium">Customer:</span> {selectedAccount.customerName}
                </div>
                <div>
                  <span className="font-medium">Account ID:</span> #{selectedAccount.accountId}
                </div>
              </div>
            </div>

            {paymentStatusLoading ? (
              <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
              </div>
            ) : (
              <div className="overflow-y-auto max-h-[60vh]">
                {/* Calendar Navigation */}
                <div className="flex justify-between items-center mb-4 p-3 bg-gray-50 rounded-lg">
                  <button
                    onClick={() => navigateMonth('prev')}
                    className="btn btn-secondary p-2"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <h3 className="text-lg font-semibold">
                    {currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                  </h3>
                  <button
                    onClick={() => navigateMonth('next')}
                    className="btn btn-secondary p-2"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>

                {/* Calendar Grid */}
                <div className="bg-white rounded-lg border border-gray-200">
                  {/* Weekday Headers */}
                  <div className="grid grid-cols-7 bg-gray-50">
                    {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                      <div key={day} className="p-2 text-center text-xs font-semibold text-gray-600 border border-gray-200">
                        {day}
                      </div>
                    ))}
                  </div>
                  
                  {/* Calendar Days */}
                  <div className="grid grid-cols-7">
                    {renderCalendar()}
                  </div>
                </div>

                {/* Summary */}
                <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="font-medium">Total Paid Days:</span>{' '}
                      <span className="text-green-600 font-semibold">
                        {getMonthlySummary().paidDays}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Total Unpaid Days:</span>{' '}
                      <span className="text-orange-600 font-semibold">
                        {getMonthlySummary().unpaidDays}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Expected This Month:</span>{' '}
                      <span className="text-blue-600 font-semibold">
                        {formatCurrency(getMonthlySummary().expectedAmount)}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Paid This Month:</span>{' '}
                      <span className="text-green-600 font-semibold">
                        {formatCurrency(getMonthlySummary().totalPaidAmount)}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Remaining This Month:</span>{' '}
                      <span className="text-red-600 font-semibold">
                        {formatCurrency(getMonthlySummary().remainingAmount)}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Days Passed This Month:</span>{' '}
                      <span className="text-gray-600 font-semibold">
                        {getMonthlySummary().daysPassed}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            <div className="flex justify-end mt-4">
              <button
                onClick={() => setShowPaymentStatusModal(false)}
                className="btn btn-primary"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {showColorPicker && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">Color Settings</h2>
            
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Paid Days Color
              </label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { name: 'Green', value: 'bg-green-600 border-green-700 text-white' },
                  { name: 'Blue', value: 'bg-blue-600 border-blue-700 text-white' },
                  { name: 'Turquoise', value: 'bg-teal-600 border-teal-700 text-white' },
                  { name: 'Purple', value: 'bg-purple-600 border-purple-700 text-white' },
                  { name: 'Indigo', value: 'bg-indigo-600 border-indigo-700 text-white' },
                  { name: 'Emerald', value: 'bg-emerald-600 border-emerald-700 text-white' },
                ].map((color) => (
                  <button
                    key={color.value}
                    onClick={() => setPaidColor(color.value)}
                    className={`p-3 rounded border-2 text-sm font-medium transition-all ${
                      paidColor === color.value ? 'ring-2 ring-offset-2 ring-blue-500' : ''
                    } ${color.value}`}
                  >
                    {color.name}
                  </button>
                ))}
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Unpaid Days Color
              </label>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { name: 'Light Peach', value: 'bg-orange-200 border-orange-300 text-gray-800' },
                  { name: 'Light Red', value: 'bg-red-200 border-red-300 text-gray-800' },
                  { name: 'Light Yellow', value: 'bg-yellow-200 border-yellow-300 text-gray-800' },
                  { name: 'Light Blue', value: 'bg-blue-200 border-blue-300 text-gray-800' },
                  { name: 'Light Gray', value: 'bg-gray-200 border-gray-300 text-gray-800' },
                  { name: 'Light Purple', value: 'bg-purple-200 border-purple-300 text-gray-800' },
                ].map((color) => (
                  <button
                    key={color.value}
                    onClick={() => setUnpaidColor(color.value)}
                    className={`p-3 rounded border-2 text-sm font-medium transition-all ${
                      unpaidColor === color.value ? 'ring-2 ring-offset-2 ring-blue-500' : ''
                    } ${color.value}`}
                  >
                    {color.name}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex justify-end gap-3">
              <button
                onClick={() => {
                  setPaidColor('bg-green-600 border-green-700 text-white');
                  setUnpaidColor('bg-orange-200 border-orange-300 text-gray-800');
                }}
                className="btn btn-secondary"
              >
                Reset to Default
              </button>
              <button
                onClick={() => setShowColorPicker(false)}
                className="btn btn-primary"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Collections;
