import type { WorkspaceProps } from '@teamflow/shared';
import './style.css';

export default function StakeholderRemote({ user }: WorkspaceProps) {
  return (
    <section className="workspace-card">
      <p className="workspace-label">Stakeholder workspace</p>
      <h1>Project updates</h1>
      <p>Welcome, {user.email}. Your project submissions and status will appear here.</p>
      <div className="empty-state">You do not have any project updates yet.</div>
    </section>
  );
}
