import type { WorkspaceProps } from '@teamflow/shared';
import './style.css';

export default function TeamRemote({ user }: WorkspaceProps) {
  return (
    <section className="workspace-card">
      <p className="workspace-label">Team workspace</p>
      <h1>Projects and work</h1>
      <p>Welcome, {user.email}. Your project team tools will appear here.</p>
      <div className="empty-state">No team work is available yet.</div>
    </section>
  );
}
