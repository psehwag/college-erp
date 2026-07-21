import { useCallback, useRef, useState } from 'react';
import { courseAPI } from '../services/api';

/**
 * Lazily fetches and caches Subject objects by id so pages can show
 * real subject names instead of "Subject #3". Call ensureLoaded([...ids])
 * once you know which subject ids you need, then read subjects[id].name.
 */
export default function useSubjectMap() {
  const [subjects, setSubjects] = useState({});
  const inFlight = useRef(new Set());

  const ensureLoaded = useCallback((ids) => {
    const unique = [...new Set((ids || []).filter(Boolean))];
    const toFetch = unique.filter(id => !subjects[id] && !inFlight.current.has(id));
    if (toFetch.length === 0) return;

    toFetch.forEach(id => inFlight.current.add(id));

    Promise.all(toFetch.map(id =>
      courseAPI.getSubjectById(id).then(r => ({ id, subject: r.data.data }))
        .catch(() => ({ id, subject: null }))
    )).then(results => {
      setSubjects(prev => {
        const next = { ...prev };
        results.forEach(({ id, subject }) => {
          next[id] = subject || { id, name: `Subject #${id}` };
          inFlight.current.delete(id);
        });
        return next;
      });
    });
  }, [subjects]);

  const getName = useCallback((id) => subjects[id]?.name || `Subject #${id}`, [subjects]);

  return { subjects, ensureLoaded, getName };
}
