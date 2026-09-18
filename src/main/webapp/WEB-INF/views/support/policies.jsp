<%@ include file="header.jspf" %>
<p class="eyebrow">KNOWLEDGE FOR CUSTOMER CARE</p><h1>상담 정책</h1><p class="muted">검토한 정책만 활성화해 주세요. 활성 정책은 다음 AI 초안 생성부터 반영됩니다.</p>
<div class="support-notice">기존 상품 화면의 안내와 일치하도록 작성해 주세요. 정책 변경은 이미 생성된 초안에 자동 반영되지 않습니다.</div>
<c:if test="${not empty failedPolicy}"><div class="support-panel"><h2>저장하지 못한 입력</h2><p><c:out value="${failedPolicy.title}"/></p><div class="preserve"><c:out value="${failedPolicy.content}"/></div><p class="help">아래 최신 정책을 확인한 뒤 이 내용을 복사해서 수정할 수 있습니다.</p></div></c:if>
<div class="policy-grid">
<c:forEach var="policy" items="${policies}"><form method="post" class="support-panel support-form" data-submit-lock>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${policy.id}"><input type="hidden" name="version" value="${policy.version}">
<p class="eyebrow">POLICY #${policy.id} · VERSION ${policy.version}</p>
<label for="title-${policy.id}">정책 제목</label><input id="title-${policy.id}" name="title" maxlength="120" required value="<c:out value='${policy.title}'/>">
<label for="content-${policy.id}">정책 내용</label><textarea id="content-${policy.id}" name="content" rows="8" maxlength="6000" required><c:out value="${policy.content}"/></textarea>
<label class="checkbox-label"><input type="checkbox" name="active" value="true" ${policy.active ? 'checked' : ''}> AI 답변 자료로 사용</label>
<div class="form-actions"><span class="help"><fmt:formatDate value="${policy.updatedAt}" pattern="yyyy.MM.dd HH:mm"/></span><button class="support-button secondary">변경 저장</button></div>
</form></c:forEach>
<form method="post" class="support-panel support-form" data-submit-lock>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="version" value="0">
<h2>새 정책 추가</h2><label for="new-title">정책 제목</label><input id="new-title" name="title" required maxlength="120" placeholder="예: 교환·반품 안내">
<label for="new-content">정책 내용</label><textarea id="new-content" name="content" rows="8" required maxlength="6000" placeholder="실제 운영하는 정책의 조건과 예외를 적어 주세요."></textarea>
<label class="checkbox-label"><input type="checkbox" name="active" value="true"> 검토 완료 · AI 답변 자료로 사용</label><button class="support-button">정책 추가 →</button>
</form></div></main></body></html>
