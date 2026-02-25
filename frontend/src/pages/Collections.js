import React, { useState, useEffect } from 'react';
import { collectionAPI, accountAPI } from '../api/api';
import { Plus, Eye, Search, Calendar, TrendingUp, X, CheckCircle, AlertCircle, ChevronLeft, ChevronRight, FileText } from 'lucide-react';

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
  const [accountSearchQuery, setAccountSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showAccountDropdown, setShowAccountDropdown] = useState(false);
  const [selectedAccountDetails, setSelectedAccountDetails] = useState(null);
  const [advancePayments, setAdvancePayments] = useState({});
  const [showTransactionsModal, setShowTransactionsModal] = useState(false);
  const [transactions, setTransactions] = useState([]);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [selectedTransactionAccount, setSelectedTransactionAccount] = useState(null);

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

  useEffect(() => {
    // Refetch payment status when month changes
    if (selectedAccount && showPaymentStatusModal) {
      fetchPaymentStatus(selectedAccount.accountId);
    }
  }, [currentMonth]);

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

  const handleAccountSearch = async (query) => {
    setAccountSearchQuery(query);
    if (query.trim().length < 2) {
      setSearchResults([]);
      setShowAccountDropdown(false);
      return;
    }

    try {
      const results = await accountAPI.search(query);
      setSearchResults(Array.isArray(results) ? results : []);
      setShowAccountDropdown(true);
    } catch (error) {
      console.error('Error searching accounts:', error);
      setSearchResults([]);
    }
  };

  const selectAccount = (account) => {
    setSelectedAccountDetails(account);
    setFormData({...formData, accountId: account.id});
    setAccountSearchQuery(`${account.accountNumber} - ${account.customerName}`);
    setShowAccountDropdown(false);
    setSearchResults([]);
  };

  const clearAccountSelection = () => {
    setSelectedAccountDetails(null);
    setFormData({...formData, accountId: ''});
    setAccountSearchQuery('');
    setSearchResults([]);
    setShowAccountDropdown(false);
  };

  const fetchTransactions = async (accountId) => {
    try {
      setTransactionsLoading(true);
      const data = await collectionAPI.getTransactionsByAccount(accountId);
      setTransactions(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching transactions:', error);
      setTransactions([]);
    } finally {
      setTransactionsLoading(false);
    }
  };

  const fetchPaymentStatus = async (accountId) => {
    try {
      setPaymentStatusLoading(true);
      
      // First fetch account details to get start date
      const accountDetails = await accountAPI.getById(accountId);
      console.log('Account details:', accountDetails);
      
      // Set selected account with start date
      setSelectedAccount(prev => ({
        ...prev,
        startDate: accountDetails.startDate
      }));
      
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
      
      // Fetch monthly summary for the selected calendar month
      const month = currentMonth.getMonth() + 1;
      const year = currentMonth.getFullYear();
      
      try {
        const monthlySummary = await collectionAPI.getMonthlyPaymentSummary(accountId, month, year);
        console.log('Monthly summary for selected month:', monthlySummary);
        // Store monthlyKist for calculations
        if (monthlySummary && monthlySummary.monthlyKist) {
          // Calculate daily amount from monthlyKist and days in month
          const daysInMonth = new Date(year, month, 0).getDate();
          const dailyAmount = monthlySummary.monthlyKist / daysInMonth;
          // Store it for use in calendar
          window.currentDailyAmount = dailyAmount;
          window.monthlyKist = monthlySummary.monthlyKist;
          // Store advance payment data
          window.advanceData = {
            advanceFromPreviousMonths: monthlySummary.advanceFromPreviousMonths || 0,
            currentMonthAdvance: monthlySummary.currentMonthAdvance || 0,
            adjustedExpectedAmount: monthlySummary.adjustedExpectedAmount || monthlySummary.monthlyKist,
            remainingAmount: monthlySummary.remainingAmount || 0,
            monthsCoveredByAdvance: monthlySummary.monthsCoveredByAdvance || 0,
            advanceDetails: monthlySummary.advanceDetails || []
          };
          console.log('Set monthlyKist:', monthlySummary.monthlyKist, 'Daily amount:', dailyAmount);
          console.log('Advance data:', window.advanceData);
          console.log('Expected amount:', window.advanceData.adjustedExpectedAmount);
          console.log('Remaining amount:', window.advanceData.remainingAmount);
        } else {
          console.log('No monthlyKist found in response');
        }
        
        // Fetch previous month's advance payment (keeping for backward compatibility)
        const prevMonth = month === 1 ? 12 : month - 1;
        const prevYear = month === 1 ? year - 1 : year;
        
        try {
          const prevMonthSummary = await collectionAPI.getMonthlyPaymentSummary(accountId, prevMonth, prevYear);
          if (prevMonthSummary) {
            // Calculate advance from previous month
            const prevMonthDaysInMonth = new Date(prevYear, prevMonth, 0).getDate();
            const prevMonthExpected = prevMonthSummary.monthlyKist || 0;
            
            // Get payment status for previous month to calculate advance
            const prevMonthPaymentResponse = await collectionAPI.getPaymentStatus(accountId);
            let prevMonthPaymentData = prevMonthPaymentResponse;
            if (prevMonthPaymentResponse.data && Array.isArray(prevMonthPaymentResponse.data)) {
              prevMonthPaymentData = prevMonthPaymentResponse.data;
            } else if (prevMonthPaymentResponse.data && prevMonthPaymentResponse.data.data && Array.isArray(prevMonthPaymentResponse.data.data)) {
              prevMonthPaymentData = prevMonthPaymentResponse.data.data;
            }
            
            let prevMonthTotalPaid = 0;
            if (Array.isArray(prevMonthPaymentData)) {
              prevMonthPaymentData.forEach(payment => {
                const paymentDate = new Date(payment.date);
                if (paymentDate.getMonth() + 1 === prevMonth && paymentDate.getFullYear() === prevYear) {
                  prevMonthTotalPaid += payment.paidAmount || 0;
                }
              });
            }
            
            const prevMonthAdvance = prevMonthTotalPaid > prevMonthExpected ? prevMonthTotalPaid - prevMonthExpected : 0;
            
            setAdvancePayments(prev => ({
              ...prev,
              [`${accountId}-${year}-${month}`]: prevMonthAdvance
            }));
            
            console.log('Previous month advance:', prevMonthAdvance);
          }
        } catch (error) {
          console.log('Could not fetch previous month advance:', error);
          setAdvancePayments(prev => ({
            ...prev,
            [`${accountId}-${year}-${month}`]: 0
          }));
        }
        
      } catch (error) {
        console.log('Could not fetch monthly summary, using default daily amount');
        window.currentDailyAmount = 200; // Default fallback
        window.monthlyKist = 6000; // Default fallback
        window.advanceData = {
          advanceFromPreviousMonths: 0,
          currentMonthAdvance: 0,
          adjustedExpectedAmount: 6000,
          remainingAmount: 6000,
          monthsCoveredByAdvance: 0,
          advanceDetails: []
        };
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

  const handleViewTransactions = (collection) => {
    setSelectedTransactionAccount(collection);
    setShowTransactionsModal(true);
    fetchTransactions(collection.accountId);
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
    
    // Check if this date is before the account start date
    if (selectedAccount && selectedAccount.startDate) {
      const accountStartDate = new Date(selectedAccount.startDate);
      console.log('Account start date:', accountStartDate, 'Current date:', date, 'Is before:', date < accountStartDate);
      if (date < accountStartDate) {
        // Return empty status for dates before start date
        return { paid: false, paidAmount: 0, isBeforeStartDate: true };
      }
    }
    
    return status || { paid: false, paidAmount: 0, isBeforeStartDate: false };
  };

  // Calculate monthly summary
  const getMonthlySummary = () => {
    const today = new Date();
    const daysInMonth = getDaysInMonth(currentMonth);
    const monthStart = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1);
    const monthEnd = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), daysInMonth);
    
    // Get account start date
    let accountStartDate = null;
    if (selectedAccount && selectedAccount.startDate) {
      accountStartDate = new Date(selectedAccount.startDate);
    }
    
    // Calculate effective start date for this month (max of month start and account start)
    let effectiveStartDate = monthStart;
    if (accountStartDate && accountStartDate > monthStart) {
      effectiveStartDate = accountStartDate;
    }
    
    // Calculate effective end date (min of today and month end)
    let effectiveEndDate = monthEnd;
    if (currentMonth.getMonth() === today.getMonth() && currentMonth.getFullYear() === today.getFullYear()) {
      effectiveEndDate = today;
    }
    
    // Count paid and unpaid days only in the effective date range
    let paidDays = 0;
    let unpaidDays = 0;
    let totalPaidAmount = 0;
    let daysPassed = 0;
    
    // Loop through all days in the month
    for (let day = 1; day <= daysInMonth; day++) {
      const currentDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
      
      // Check if date is within effective range
      if (currentDate >= effectiveStartDate && currentDate <= effectiveEndDate) {
        const status = getPaymentStatusForDate(currentDate);
        
        if (status.paid) {
          paidDays++;
          totalPaidAmount += status.paidAmount;
        } else if (!status.isBeforeStartDate) {
          // Only count as unpaid if it's not before start date
          unpaidDays++;
        }
        
        daysPassed++;
      }
    }
    
    // Also check for paid days after today but within month (advance payments)
    if (currentMonth.getMonth() === today.getMonth() && currentMonth.getFullYear() === today.getFullYear()) {
      for (let day = today.getDate() + 1; day <= daysInMonth; day++) {
        const currentDate = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
        
        // Only check if within effective start date
        if (currentDate >= effectiveStartDate) {
          const status = getPaymentStatusForDate(currentDate);
          
          if (status.paid) {
            paidDays++;
            totalPaidAmount += status.paidAmount;
          }
        }
      }
    }
    
    // Expected This Month: Calculate based on effective days passed
    const baseExpectedAmount = window.monthlyKist || 0;
    const advanceData = window.advanceData || {};
    
    // Get advance payment from previous month
    const month = currentMonth.getMonth() + 1;
    const year = currentMonth.getFullYear();
    const advanceFromPrevMonth = advancePayments[`${selectedAccount?.accountId}-${year}-${month}`] || 0;
    
    // Use backend advance data if available
    const advanceFromPreviousMonths = advanceData.advanceFromPreviousMonths || advanceFromPrevMonth;
    
    // Calculate daily amount
    const dailyAmount = baseExpectedAmount / daysInMonth;
    
    // Calculate expected amount: (Daily amount * Days passed) - Advance from previous months
    const rawExpectedAmount = dailyAmount * daysPassed;
    const expectedAmount = Math.max(0, rawExpectedAmount - advanceFromPreviousMonths);
    
    // Remaining This Month
    const remainingAmount = Math.max(0, expectedAmount - totalPaidAmount);
    
    // Advance Payment in this month
    const advancePayment = advanceData.currentMonthAdvance || 
                         ((totalPaidAmount - expectedAmount < 0) ? 0 : (totalPaidAmount - expectedAmount));
    
    return {
      paidDays,
      unpaidDays,
      expectedAmount,
      totalPaidAmount,
      remainingAmount,
      advancePayment,
      advanceFromPreviousMonths,
      monthsCoveredByAdvance: advanceData.monthsCoveredByAdvance || 0,
      advanceDetails: advanceData.advanceDetails || [],
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
    
    // Refetch payment status for the new month
    if (selectedAccount) {
      setTimeout(() => {
        fetchPaymentStatus(selectedAccount.accountId);
      }, 100);
    }
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
      
      // Determine if the date should be shown as blank (before start date or after today)
      const isBlank = paymentInfo.isBeforeStartDate || currentDate > new Date();
      
      days.push(
        <div
          key={day}
          className={`p-2 border-2 min-h-[80px] ${
            isBlank
              ? 'bg-white border-gray-200 text-gray-400'
              : paymentInfo.paid
                ? paidColor
                : unpaidColor
          } ${
            isToday ? 'ring-4 ring-blue-500 ring-opacity-50' : ''
          }`}
        >
          <div className="text-xs font-bold mb-1">{day}</div>
          {!isBlank && (
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
          )}
          {isBlank && (
            <div className="text-xs text-gray-400">
              <div className="text-center mt-3">
                {/* Empty for dates before start date or after today */}
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
    
    // Validate collection date is not in the future
    const collectionDate = new Date(formData.collectionDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (collectionDate > today) {
      alert('Cannot add collection for future dates. Please select today or a past date.');
      return;
    }
    
    try {
      await collectionAPI.create(formData);
      setShowModal(false);
      setFormData({ accountId: '', collectedAmount: '', collectionDate: new Date().toISOString().split('T')[0] });
      setAccountSearchQuery('');
      setSelectedAccountDetails(null);
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
                  <td className="table-cell font-semibold text-green-600">{formatCurrency(collection.collectedAmount)}</td>
                  <td className="table-cell">{collection.month}</td>
                  <td className="table-cell">{collection.year}</td>
                  <td className="table-cell">
                    <div className="flex gap-2">
                      <button 
                        onClick={() => handleViewPaymentStatus(collection)}
                        className="btn btn-secondary p-2"
                        title="View Payment Status"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleViewTransactions(collection)}
                        className="btn btn-secondary p-2"
                        title="View All Transactions"
                      >
                        <FileText className="w-4 h-4" />
                      </button>
                    </div>
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
                  Search Account *
                </label>
                <div className="relative">
                  <div className="relative">
                    <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                    <input
                      type="text"
                      required
                      placeholder="Search by account number or customer name..."
                      className="input-field pl-10 pr-10"
                      value={accountSearchQuery}
                      onChange={(e) => handleAccountSearch(e.target.value)}
                      onFocus={() => accountSearchQuery.trim().length >= 2 && setShowAccountDropdown(true)}
                    />
                    {accountSearchQuery && (
                      <button
                        type="button"
                        onClick={clearAccountSelection}
                        className="absolute right-3 top-3 text-gray-400 hover:text-gray-600"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  
                  {showAccountDropdown && searchResults.length > 0 && (
                    <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                      {searchResults.map((account) => (
                        <div
                          key={account.id}
                          onClick={() => selectAccount(account)}
                          className="px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                        >
                          <div className="font-medium text-gray-900">{account.accountNumber}</div>
                          <div className="text-sm text-gray-600">{account.customerName}</div>
                          <div className="text-xs text-gray-500 mt-1">
                            📱 {account.mobileNumber} | ✉️ {account.email}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {selectedAccountDetails && (
                <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
                  <h4 className="text-sm font-medium text-blue-900 mb-2">Selected Account Details:</h4>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="font-medium">Account:</span> {selectedAccountDetails.accountNumber}
                    </div>
                    <div>
                      <span className="font-medium">Name:</span> {selectedAccountDetails.customerName}
                    </div>
                    <div>
                      <span className="font-medium">Mobile:</span> {selectedAccountDetails.mobileNumber}
                    </div>
                    <div>
                      <span className="font-medium">Email:</span> {selectedAccountDetails.email}
                    </div>
                  </div>
                </div>
              )}
              
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Collected Amount *
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-3 text-gray-500 z-10">₹</span>
                  <input
                    type="number"
                    required
                    min="1"
                    step="0.01"
                    className="input-field pl-8 [appearance:textfield] [&::-webkit-outer-spin-button]:hidden [&::-webkit-inner-spin-button]:hidden"
                    value={formData.collectedAmount}
                    onChange={(e) => setFormData({...formData, collectedAmount: e.target.value})}
                    style={{
                      MozAppearance: 'textfield',
                      WebkitAppearance: 'none'
                    }}
                  />
                </div>
              </div>
              
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Collection Date *
                </label>
                <input
                  type="date"
                  required
                  max={new Date().toISOString().split('T')[0]}
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
                  {/* Conditional heading for advance payment from previous month */}
                  {getMonthlySummary().advanceFromPreviousMonths > 0 && (
                    <div className="mb-3 p-2 bg-green-100 border border-green-300 rounded-md">
                      <p className="text-sm font-medium text-green-800">
                        Your previous advanced amount of {formatCurrency(getMonthlySummary().advanceFromPreviousMonths)} added to this month
                        {getMonthlySummary().monthsCoveredByAdvance > 0 && (
                          <span className="block text-xs mt-1">
                            Covers {getMonthlySummary().monthsCoveredByAdvance} month{getMonthlySummary().monthsCoveredByAdvance > 1 ? 's' : ''} of payments
                          </span>
                        )}
                      </p>
                    </div>
                  )}
                  
                  {/* Conditional heading for no advance payment */}
                  {getMonthlySummary().advanceFromPreviousMonths === 0 && (
                    <div className="mb-3 p-2 bg-gray-100 border border-gray-300 rounded-md">
                      <p className="text-sm font-medium text-gray-600">
                        You did not pay any advance amount
                      </p>
                    </div>
                  )}
                  
                  {/* Advance details */}
                  {getMonthlySummary().advanceDetails && getMonthlySummary().advanceDetails.length > 0 && (
                    <div className="mb-3 p-2 bg-blue-50 border border-blue-200 rounded-md">
                      <p className="text-sm font-medium text-blue-800 mb-2">Advance Payment Details:</p>
                      {getMonthlySummary().advanceDetails.map((detail, index) => (
                        <div key={index} className="text-xs text-blue-700 mb-1">
                          • {detail.originalMonth}/{detail.originalYear}: {formatCurrency(detail.appliedThisMonth)} applied (remaining: {formatCurrency(detail.remainingAdvance)})
                        </div>
                      ))}
                    </div>
                  )}
                  
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
                      {getMonthlySummary().advanceFromPreviousMonths > 0 && (
                        <div className="text-xs text-gray-500">
                          (Base: {formatCurrency(window.monthlyKist || 0)} - Advance: {formatCurrency(getMonthlySummary().advanceFromPreviousMonths)})
                        </div>
                      )}
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
                    <div>
                    <span className="font-medium">Advance Payment in this month:</span>{' '}
                    <span className="text-gray-600 font-semibold">
                    {formatCurrency(getMonthlySummary().advancePayment)}
                     </span>
                     </div>
                    <div>
                      <span className="font-medium">Months Covered:</span>{' '}
                      <span className="text-purple-600 font-semibold">
                        {getMonthlySummary().monthsCoveredByAdvance}
                      </span>
                    </div>
                  </div>
                  
                  {/* Customer Satisfaction Button */}
                  <div className="mt-4 flex justify-center">
                    <button
                      onClick={() => {
                        const satisfaction = getMonthlySummary().remainingAmount <= 0 ? 'excellent' : 
                                          getMonthlySummary().remainingAmount <= 1000 ? 'good' : 'needs_attention';
                        const message = getMonthlySummary().monthsCoveredByAdvance > 0 
                          ? `Customer Satisfaction: ${satisfaction.toUpperCase()}\n\nPayment Status: ${getMonthlySummary().remainingAmount <= 0 ? 'All dues cleared!' : 'Pending payments exist'}\n\n🎉 ${getMonthlySummary().monthsCoveredByAdvance} month${getMonthlySummary().monthsCoveredByAdvance > 1 ? 's' : ''} covered by advance payments!`
                          : `Customer Satisfaction: ${satisfaction.toUpperCase()}\n\nPayment Status: ${getMonthlySummary().remainingAmount <= 0 ? 'All dues cleared!' : 'Pending payments exist'}`;
                        alert(message);
                      }}
                      className="btn btn-primary flex items-center gap-2"
                    >
                      <CheckCircle className="w-4 h-4" />
                      Check Customer Satisfaction
                    </button>
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

      {/* Transactions Modal */}
      {showTransactionsModal && selectedTransactionAccount && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-5xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold">
                All Transactions - {selectedTransactionAccount.customerName}
              </h2>
              <button
                onClick={() => setShowTransactionsModal(false)}
                className="btn btn-secondary p-2"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="font-medium">Account ID:</span> {selectedTransactionAccount.accountId}
                </div>
                <div>
                  <span className="font-medium">Customer:</span> {selectedTransactionAccount.customerName}
                </div>
                <div>
                  <span className="font-medium">Total Transactions:</span> {transactions.length}
                </div>
              </div>
            </div>

            {transactionsLoading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-2 text-gray-600">Loading transactions...</p>
              </div>
            ) : transactions.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                No transactions found for this account
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="table w-full">
                  <thead className="table-header">
                    <tr>
                      <th className="table-header-cell">#</th>
                      <th className="table-header-cell">Collection ID</th>
                      <th className="table-header-cell">Date</th>
                      <th className="table-header-cell">Amount</th>
                      <th className="table-header-cell">Month</th>
                      <th className="table-header-cell">Year</th>
                    </tr>
                  </thead>
                  <tbody className="table-body">
                    {transactions.map((transaction, index) => (
                      <tr key={transaction.collectionId} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                        <td className="table-cell">{index + 1}</td>
                        <td className="table-cell font-medium">#{transaction.collectionId}</td>
                        <td className="table-cell">{new Date(transaction.collectionDate).toLocaleDateString()}</td>
                        <td className="table-cell font-semibold text-green-600">{formatCurrency(transaction.collectedAmount)}</td>
                        <td className="table-cell">{transaction.month}</td>
                        <td className="table-cell">{transaction.year}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="flex justify-end mt-4">
              <button
                onClick={() => setShowTransactionsModal(false)}
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
