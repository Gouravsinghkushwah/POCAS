import React, { useState, useEffect } from 'react';
import { customerAPI, accountAPI, collectionAPI } from '../api/api';
import { Users, CreditCard, DollarSign, TrendingUp } from 'lucide-react';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalCustomers: 0,
    totalAccounts: 0,
    totalCollections: 0,
    totalCollected: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [customersRes, accountsRes, collectionsRes] = await Promise.all([
        customerAPI.getAll(),
        accountAPI.getAll(),
        collectionAPI.getAll(),
      ]);

      const customersArray = Array.isArray(customersRes) ? customersRes : [];
      const accountsArray = Array.isArray(accountsRes) ? accountsRes : [];
      const collectionsArray = Array.isArray(collectionsRes) ? collectionsRes : [];

      const totalCollected = collectionsArray.reduce(
        (sum, collection) => sum + parseFloat(collection.collectedAmount || 0),
        0
      );

      setStats({
        totalCustomers: customersArray.length,
        totalAccounts: accountsArray.length,
        totalCollections: collectionsArray.length,
        totalCollected: totalCollected.toFixed(2),
      });
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setStats({
        totalCustomers: 0,
        totalAccounts: 0,
        totalCollections: 0,
        totalCollected: '0.00',
      });
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    {
      title: 'Total Customers',
      value: stats.totalCustomers,
      icon: Users,
      color: 'bg-blue-500',
    },
    {
      title: 'Total Accounts',
      value: stats.totalAccounts,
      icon: CreditCard,
      color: 'bg-green-500',
    },
    {
      title: 'Total Collections',
      value: stats.totalCollections,
      icon: DollarSign,
      color: 'bg-yellow-500',
    },
    {
      title: 'Total Collected',
      value: `₹${stats.totalCollected}`,
      icon: TrendingUp,
      color: 'bg-purple-500',
    },
  ];

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statCards.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <div key={index} className="card">
              <div className="flex items-center">
                <div className={`${stat.color} p-3 rounded-full`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                  <p className="text-2xl font-semibold text-gray-900">{stat.value}</p>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Recent Collections</h2>
          <div className="text-gray-600">
            <p>Recent collections will appear here</p>
          </div>
        </div>
        
        <div className="card">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Account Status</h2>
          <div className="text-gray-600">
            <p>Account status overview will appear here</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
