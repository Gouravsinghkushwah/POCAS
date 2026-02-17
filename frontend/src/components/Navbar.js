import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, Users, CreditCard, DollarSign } from 'lucide-react';

const Navbar = () => {
  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path;
  };

  const navItems = [
    { path: '/', label: 'Dashboard', icon: Home },
    { path: '/customers', label: 'Customers', icon: Users },
    { path: '/accounts', label: 'Accounts', icon: CreditCard },
    { path: '/collections', label: 'Collections', icon: DollarSign },
  ];

  return (
    <nav className="bg-primary-600 text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <h1 className="text-xl font-bold">POCAS</h1>
            <span className="ml-2 text-sm opacity-75">Daily Collection System</span>
          </div>
          
          <div className="flex space-x-4">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${
                    isActive(item.path)
                      ? 'bg-primary-700 text-white'
                      : 'text-primary-100 hover:bg-primary-500 hover:text-white'
                  }`}
                >
                  <Icon className="w-4 h-4 mr-2" />
                  {item.label}
                </Link>
              );
            })}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
