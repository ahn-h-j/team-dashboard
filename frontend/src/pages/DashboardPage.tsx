import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-200">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
          <h1 className="text-xl font-bold text-gray-900">Team Dashboard</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {user?.name ?? user?.email}
            </span>
            <button
              onClick={handleLogout}
              className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-10">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
          <h2 className="text-2xl font-semibold text-gray-900 mb-4">
            Welcome back{user?.name ? `, ${user.name}` : ''}!
          </h2>
          <p className="text-gray-600">
            This is a placeholder dashboard page. Your project content will
            appear here.
          </p>

          {user && (
            <dl className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="rounded-lg bg-gray-50 p-4">
                <dt className="text-xs font-medium text-gray-500 uppercase tracking-wide">
                  Email
                </dt>
                <dd className="mt-1 text-sm font-medium text-gray-900">
                  {user.email}
                </dd>
              </div>
              <div className="rounded-lg bg-gray-50 p-4">
                <dt className="text-xs font-medium text-gray-500 uppercase tracking-wide">
                  Role
                </dt>
                <dd className="mt-1 text-sm font-medium text-gray-900">
                  {user.role}
                </dd>
              </div>
              <div className="rounded-lg bg-gray-50 p-4">
                <dt className="text-xs font-medium text-gray-500 uppercase tracking-wide">
                  User ID
                </dt>
                <dd className="mt-1 text-sm font-medium text-gray-900">
                  {user.id}
                </dd>
              </div>
            </dl>
          )}
        </div>
      </main>
    </div>
  );
}
